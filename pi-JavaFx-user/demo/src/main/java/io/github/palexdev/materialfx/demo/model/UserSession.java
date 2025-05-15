package io.github.palexdev.materialfx.demo.model;

import io.github.palexdev.materialfx.demo.services.GoogleAuthService;
import javafx.application.Platform;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public final class UserSession {
    private GoogleAuthService.UserInfo tempUserInfo;
    private static volatile UserSession instance;
    private static final Object LOCK = new Object();

    // Changed session timeout to 1 minute for testing
    private static final int SESSION_TIMEOUT = 30;
    private Instant lastActivityTime;
    private final ScheduledExecutorService scheduler;
    private final List<SessionListener> sessionListeners;

    private User currentUser;
    private final Set<String> privileges;

    // Role-based privilege mapping
    private static final Map<String, Set<String>> rolePrivileges = new HashMap<>();
    static {
        rolePrivileges.put("admin", Set.of("MANAGE_USERS", "VIEW_DASHBOARD", "FULL_ACCESS"));
        rolePrivileges.put("client", Set.of("VIEW_DASHBOARD", "MANAGE_PROFILE"));
    }

    private UserSession() {
        privileges = new HashSet<>();
        sessionListeners = new CopyOnWriteArrayList<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startSessionMonitor();
    }

    // Modified to check every 10 seconds instead of every minute
    private void startSessionMonitor() {
        scheduler.scheduleAtFixedRate(() -> {
            if (isLoggedIn() && isSessionExpired()) {
                Platform.runLater(this::handleSessionTimeout);
            }
        }, 0, 10, TimeUnit.SECONDS);  // Check every 10 seconds
    }

    private boolean isSessionExpired() {
        if (lastActivityTime == null) return false;
        long minutesSinceLastActivity = Duration.between(lastActivityTime, Instant.now()).toMinutes();
        // Added debug logging
        System.out.println("Minutes since last activity: " + minutesSinceLastActivity);
        return minutesSinceLastActivity >= SESSION_TIMEOUT;
    }

    private void handleSessionTimeout() {
        System.out.println("Session expired at: " + Instant.now()); // Debug log
        logout();
        notifySessionExpired();
    }

    // Rest of the UserSession code remains the same...
    // (Include all other methods from your original UserSession class)

    public static UserSession getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new UserSession();
                }
            }
        }
        return instance;
    }



    public void initSession(User user) {
        synchronized (LOCK) {
            this.currentUser = user;
            initPrivilegesForRole(user.getRole());
            updateLastActivityTime();
            System.out.println("Session initialized at: " + lastActivityTime); // Debug log
        }
    }

    private void initPrivilegesForRole(String role) {
        privileges.clear();
        privileges.addAll(rolePrivileges.getOrDefault(role.toLowerCase(), Set.of("BASIC_ACCESS")));
    }

    public void updateLastActivityTime() {
        lastActivityTime = Instant.now();
        System.out.println("Activity time updated to: " + lastActivityTime); // Debug log
    }

    public void addSessionListener(SessionListener listener) {
        sessionListeners.add(listener);
    }

    public void removeSessionListener(SessionListener listener) {
        sessionListeners.remove(listener);
    }

    private void notifySessionExpired() {
        for (SessionListener listener : sessionListeners) {
            listener.onSessionExpired();
        }
    }

    public boolean hasPrivilege(String privilege) {
        updateLastActivityTime();
        return privileges.contains(privilege);
    }

    public Set<String> getPrivileges() {
        updateLastActivityTime();
        return Collections.unmodifiableSet(privileges);
    }

    public void logout() {
        synchronized (LOCK) {
            System.out.println("Logging out user at: " + Instant.now()); // Debug log
            this.currentUser = null ;
            privileges.clear();
            lastActivityTime = null;
            sessionListeners.clear();

        }
    }

    public boolean isLoggedIn() {
        return currentUser != null ;
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public interface SessionListener {
        void onSessionExpired();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void updateUser(User user) {
        synchronized (LOCK) {
            this.currentUser = user;
            updateLastActivityTime();
        }
    }
    public void setTempUserInfo(GoogleAuthService.UserInfo userInfo) {
        this.tempUserInfo = userInfo;
    }

    public GoogleAuthService.UserInfo getTempUserInfo() {
        return tempUserInfo;
    }

    public void clearTempUserInfo() {
        this.tempUserInfo = null;
    }


}