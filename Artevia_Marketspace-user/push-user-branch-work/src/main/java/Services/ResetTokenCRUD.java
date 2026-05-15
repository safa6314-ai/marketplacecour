package Services;

import Utils.MyBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ResetTokenCRUD {

    private final Connection conn;

    public ResetTokenCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    public void createToken(int userId, String token, LocalDateTime expiresAt) throws SQLException {
        markOldTokensUsed(userId);

        String req = "INSERT INTO password_reset_tokens (user_id, token, expires_at, used) VALUES (?, ?, ?, false)";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, userId);
        pst.setString(2, token);
        pst.setTimestamp(3, Timestamp.valueOf(expiresAt));
        pst.executeUpdate();
    }

    public Integer getValidTokenUserId(String email, String token) throws SQLException {
        String req = "SELECT prt.user_id "
                + "FROM password_reset_tokens prt "
                + "JOIN users u ON u.id = prt.user_id "
                + "WHERE u.email = ? "
                + "AND prt.token = ? "
                + "AND prt.used = false "
                + "AND prt.expires_at > NOW() "
                + "ORDER BY prt.id DESC "
                + "LIMIT 1";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, email);
        pst.setString(2, token);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getInt("user_id");
        }

        return null;
    }

    public void markTokenUsed(int userId, String token) throws SQLException {
        String req = "UPDATE password_reset_tokens SET used = true WHERE user_id = ? AND token = ?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, userId);
        pst.setString(2, token);
        pst.executeUpdate();
    }

    public boolean isTokenValid(int userId, String token) throws SQLException {
        String req = "SELECT id FROM password_reset_tokens "
                + "WHERE user_id = ? AND token = ? AND used = false AND expires_at > NOW() "
                + "ORDER BY id DESC LIMIT 1";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, userId);
        pst.setString(2, token);
        ResultSet rs = pst.executeQuery();

        return rs.next();
    }

    private void markOldTokensUsed(int userId) throws SQLException {
        String req = "UPDATE password_reset_tokens SET used = true WHERE user_id = ? AND used = false";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, userId);
        pst.executeUpdate();
    }
}

