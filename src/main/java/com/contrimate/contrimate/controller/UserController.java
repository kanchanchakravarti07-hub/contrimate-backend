package com.contrimate.contrimate.controller;

import com.contrimate.contrimate.entity.User;
import com.contrimate.contrimate.entity.Friendship;
import com.contrimate.contrimate.entity.Notification;
import com.contrimate.contrimate.repository.UserRepository;
import com.contrimate.contrimate.repository.FriendshipRepository;
import com.contrimate.contrimate.repository.NotificationRepository;
import com.contrimate.contrimate.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // --- 1. SEND OTP ---
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        try {
            String cleanEmail = email.trim().toLowerCase();
            String emailRegex = "^[A-Za-z0-9+_.-]+@gmail\\.com$";
            if (!Pattern.matches(emailRegex, cleanEmail)) {
                return ResponseEntity.badRequest().body("Error: Sirf valid '@gmail.com' allow hai!");
            }
            if (userRepository.findByEmail(cleanEmail).isPresent()) {
                return ResponseEntity.badRequest().body("Error: Ye Email pehle se registered hai! Login karo.");
            }
            emailService.sendOtp(cleanEmail);
            return ResponseEntity.ok("OTP bhej diya gaya hai: " + cleanEmail);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Email error: " + e.getMessage());
        }
    }

    // --- 2. SIGNUP / ADD USER ---
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        try {
            String email = user.getEmail().trim().toLowerCase();
            
            if (!emailService.verifyOtp(email, user.getOtp())) {
                return ResponseEntity.badRequest().body("Error: OTP galat hai ya expire ho gaya hai!");
            }
            if (!isValidUpi(user.getUpiId())) {
                return ResponseEntity.badRequest().body("Error: Invalid UPI ID! (e.g. name@oksbi)");
            }
            if (!Pattern.matches("^[A-Za-z0-9+_.-]+@gmail\\.com$", email)) {
                return ResponseEntity.badRequest().body("Error: Sirf valid '@gmail.com' ID allow hai!");
            }
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Error: Email already registered.");
            }

            user.setEmail(email);
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server Error: " + e.getMessage());
        }
    }

    private boolean isValidUpi(String upiId) {
        if (upiId == null || upiId.isEmpty()) return false;
        if (!upiId.matches("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$")) return false;
        String[] validHandles = { "okaxis", "oksbi", "okhdfcbank", "okicici", "paytm", "ybl", "ibl", "axl", "upi", "postbank", "kotak", "idfcbank" };
        for (String handle : validHandles) {
            if (upiId.toLowerCase().endsWith("@" + handle)) return true;
        }
        return false;
    }

    // --- 3. LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData) {
        Optional<User> user = userRepository.findByEmail(loginData.getEmail());
        if (user.isPresent() && user.get().getPassword().equals(loginData.getPassword())) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(401).body("Galat Email ya Password!");
        }
    }

    // --- 4. GET ALL USERS ---
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // --- 5. DELETE USER ---
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User Deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cannot delete user involved in expenses!");
        }
    }

    // --- 6. SEND FRIEND REQUEST ---
    @PostMapping("/add-friend")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, String> request) {
        try {
            String myEmail = request.get("myEmail").trim().toLowerCase();
            String friendEmail = request.get("friendEmail").trim().toLowerCase();

            if (myEmail.equals(friendEmail)) return ResponseEntity.badRequest().body("Error: Khud ko request nahi bhej sakte!");

            Optional<User> me = userRepository.findByEmail(myEmail);
            Optional<User> friend = userRepository.findByEmail(friendEmail);

            if (friend.isEmpty()) return ResponseEntity.status(404).body("Error: User nahi mila!");

            if (friendshipRepository.existsByUsers(me.get(), friend.get())) {
                return ResponseEntity.badRequest().body("Error: Request pehle se bheji hui hai ya dost hain!");
            }

            // Create Friendship Entry (Pending)
            Friendship f = new Friendship();
            f.setUser(me.get());
            f.setFriend(friend.get());
            f.setStatus("PENDING"); 
            friendshipRepository.save(f);

            // Create Notification
            Notification n = new Notification();
            n.setUserId(friend.get().getId()); 
            n.setMessage(me.get().getName() + " sent you a friend request! 👋");
            n.setIsRead(false);
            notificationRepository.save(n);

            return ResponseEntity.ok("Friend Request Sent! 📩");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // --- 7. ACCEPT FRIEND REQUEST ---
    @PostMapping("/accept-friend")
    public ResponseEntity<?> acceptRequest(@RequestBody Map<String, Long> request) {
        Long requestId = request.get("requestId");
        Optional<Friendship> f = friendshipRepository.findById(requestId);

        if (f.isPresent()) {
            Friendship friendship = f.get();
            friendship.setStatus("ACCEPTED"); 
            friendshipRepository.save(friendship);
            return ResponseEntity.ok("Friend Request Accepted! ✅");
        }
        return ResponseEntity.badRequest().body("Request not found");
    }

    // --- 8. GET MY FRIENDS (Only Accepted) ---
    @GetMapping("/my-friends")
    public ResponseEntity<?> getMyFriends(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return ResponseEntity.status(404).build();

        List<Friendship> friendships = friendshipRepository.findAllAcceptedFriends(user.get());
        
        List<User> friends = friendships.stream().map(f -> {
            if (f.getUser().getId().equals(user.get().getId())) return f.getFriend(); 
            else return f.getUser(); 
        }).toList();

        return ResponseEntity.ok(friends);
    }

    // --- 9. GET PENDING REQUESTS (Incoming) ---
    @GetMapping("/pending-requests")
    public ResponseEntity<?> getPendingRequests(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return ResponseEntity.status(404).build();

        List<Friendship> requests = friendshipRepository.findByFriendAndStatus(user.get(), "PENDING");
        return ResponseEntity.ok(requests);
    }

    // --- 🔥 10. GET SENT REQUESTS (Outgoing) ---
    @GetMapping("/sent-requests")
    public ResponseEntity<?> getSentRequests(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return ResponseEntity.status(404).build();

        List<Friendship> sent = friendshipRepository.findByUserAndStatus(user.get(), "PENDING");
        return ResponseEntity.ok(sent);
    }

    // --- 🔥 11. REMOVE FRIEND OR CANCEL REQUEST ---
    @PostMapping("/remove-friend")
    public ResponseEntity<?> removeFriend(@RequestBody Map<String, String> request) {
        String myEmail = request.get("myEmail");
        Long otherId = Long.valueOf(request.get("friendId"));

        Optional<User> me = userRepository.findByEmail(myEmail);
        Optional<User> other = userRepository.findById(otherId);

        if (me.isPresent() && other.isPresent()) {
            User u1 = me.get();
            User u2 = other.get();

            // Case A: Maine request bheji thi (Cancel Request)
            Optional<Friendship> f1 = friendshipRepository.findByUserAndFriend(u1, u2);
            if (f1.isPresent()) {
                friendshipRepository.delete(f1.get());
                return ResponseEntity.ok("Request Cancelled / Friend Removed");
            }

            // Case B: Usne request bheji thi (Unfriend / Reject)
            Optional<Friendship> f2 = friendshipRepository.findByUserAndFriend(u2, u1);
            if (f2.isPresent()) {
                friendshipRepository.delete(f2.get());
                return ResponseEntity.ok("Friend Removed");
            }
            
            return ResponseEntity.badRequest().body("No friendship found to delete");
        }
        return ResponseEntity.badRequest().body("User not found");
    }
}