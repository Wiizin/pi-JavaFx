package io.github.palexdev.materialfx.demo.model;

import javafx.application.Platform;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class UserSession {
    private static volatile UserSession instance;
    private static final Object LOCK = new Object();

    // Session timeout in minutes
    private static final int SESSION_TIMEOUT = 30;
    private Instant lastActivityTime;
    private final ScheduledExecutorService scheduler;
    private List<SessionListener> sessionListeners;

    private String userName;
    private String firstName;
    private String role;
    private final Set<String> privileges;

    private UserSession() {
        privileges = new HashSet<>();
        sessionListeners = new ArrayList<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startSessionMonitor();
    }

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
            this.userName = user.getEmail();
            this.firstName = user.getFirstname();
            this.role = user.getRole();
            initPrivilegesForRole(role);
            updateLastActivityTime();
        }
    }

    private void initPrivilegesForRole(String role) {
        privileges.clear();
        switch (role.toLowerCase()) {
            case "admin":
                privileges.addAll(Arrays.asList(
                        "MANAGE_USERS",
                        "VIEW_DASHBOARD",
                        "FULL_ACCESS"
                ));
                break;
            case "client":
                privileges.addAll(Arrays.asList(
                        "VIEW_DASHBOARD",
                        "MANAGE_PROFILE"
                ));
                break;
            default:
                privileges.add("BASIC_ACCESS");
        }
    }

    public void updateLastActivityTime() {
        lastActivityTime = Instant.now();
    }

    private void startSessionMonitor() {
        scheduler.scheduleAtFixedRate(() -> {
            if (isLoggedIn() && isSessionExpired()) {
                Platform.runLater(this::handleSessionTimeout);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private boolean isSessionExpired() {
        if (lastActivityTime == null) return false;
        long minutesSinceLastActivity = TimeUnit.SECONDS.toMinutes(
                Instant.now().getEpochSecond() - lastActivityTime.getEpochSecond()
        );
        return minutesSinceLastActivity >= SESSION_TIMEOUT;
    }

    private void handleSessionTimeout() {
        logout();
        notifySessionExpired();
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

    public String getUserName() {
        updateLastActivityTime();
        return userName;
    }

    public String getFirstName() {
        updateLastActivityTime();
        return firstName;
    }

    public String getRole() {
        updateLastActivityTime();
        return role;
    }

    public Set<String> getPrivileges() {
        updateLastActivityTime();
        return Collections.unmodifiableSet(privileges);
    }

    public void logout() {
        synchronized (LOCK) {
            userName = null;
            firstName = null;
            role = null;
            privileges.clear();
            lastActivityTime = null;
        }
    }

    public boolean isLoggedIn() {
        return userName != null && !userName.isEmpty();
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    // Interface for session listeners
    public interface SessionListener {
        void onSessionExpired();
    }
}