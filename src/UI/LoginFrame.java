package UI;

import UI.components.RoundedComponents.RoundedBorder;
import UI.components.RoundedComponents.RoundedButton;
import UI.components.UIConstants;
import entity.NhanVien;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import service.AuthService;
public class LoginFrame extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private RoundedButton  btnLogin;
    private JLabel         lblError;

    public LoginFrame() {
        setTitle("Đăng nhập – Hotel MS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(Color.WHITE);

        root.add(buildLeftPanel());
        root.add(buildRightPanel());

        setContentPane(root);
    }

    // ---- Panel trái: logo + branding (xanh gradient) ----
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0x3B82F6),
                    getWidth(), getHeight(), new Color(0x1E40AF));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Circles decoration
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(-60, -60, 220, 220);
                g2.fillOval(getWidth() - 80, getHeight() - 80, 200, 200);
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Logo
        JPanel logoBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("HI", (getWidth()-fm.stringWidth("HI"))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        logoBox.setOpaque(false);
        logoBox.setPreferredSize(new Dimension(72, 72));
        logoBox.setMaximumSize(new Dimension(72, 72));
        logoBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Hotel MS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Hệ thống quản lý khách sạn");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(255, 255, 255, 200));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(Box.createVerticalStrut(10));
        content.add(logoBox);
        content.add(Box.createVerticalStrut(16));
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(6));
        content.add(lblSub);
        content.add(Box.createVerticalStrut(30));

        // Features list
        String[] features = {" Quản lý phòng & đặt phòng", " Theo dõi khách hàng", " Báo cáo & thống kê"};
        for (String f : features) {
            JLabel lbl = new JLabel(f);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(new Color(255, 255, 255, 180));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(lbl);
            content.add(Box.createVerticalStrut(6));
        }

        panel.add(content);
        return panel;
    }

    // ---- Panel phải: form đăng nhập ----
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(300, 360));

        JLabel lblWelcome = new JLabel("Chào mừng trở lại!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(UIConstants.TEXT_PRIMARY);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNote = new JLabel("Vui lòng đăng nhập để tiếp tục");
        lblNote.setFont(UIConstants.FONT_BODY);
        lblNote.setForeground(UIConstants.TEXT_SECONDARY);
        lblNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(lblWelcome);
        form.add(Box.createVerticalStrut(4));
        form.add(lblNote);
        form.add(Box.createVerticalStrut(28));

        // Username
        form.add(buildFieldLabel("Tên đăng nhập"));
        form.add(Box.createVerticalStrut(4));
        txtUsername = new JTextField();
        styleField(txtUsername);
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(14));

        // Password
        form.add(buildFieldLabel("Mật khẩu"));
        form.add(Box.createVerticalStrut(4));
        txtPassword = new JPasswordField();
        styleField(txtPassword);
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(10));

        // Remember me
        JCheckBox chkRemember = new JCheckBox("Ghi nhớ đăng nhập");
        chkRemember.setFont(UIConstants.FONT_BODY);
        chkRemember.setForeground(UIConstants.TEXT_SECONDARY);
        chkRemember.setOpaque(false);
        chkRemember.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chkRemember);
        form.add(Box.createVerticalStrut(18));

        // Error label
        lblError = new JLabel(" ");
        lblError.setFont(UIConstants.FONT_SMALL);
        lblError.setForeground(UIConstants.DANGER);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblError);
        form.add(Box.createVerticalStrut(8));

        // Login button
        btnLogin = new RoundedButton("Đăng nhập", UIConstants.PRIMARY, Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(14));

        // Demo hint
        JLabel lblDemo = new JLabel("Demo: admin / 123456");
        lblDemo.setFont(UIConstants.FONT_SMALL);
        lblDemo.setForeground(UIConstants.TEXT_MUTED);
        lblDemo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblDemo);

        panel.add(form);

        // Events
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());

        return panel;
    }

    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setFont(UIConstants.FONT_BODY);
        field.setPreferredSize(new Dimension(300, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(UIConstants.BTN_RADIUS, UIConstants.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                Boolean hasError = (Boolean) field.getClientProperty("hasError");
                if (hasError == null || !hasError) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(UIConstants.BTN_RADIUS, UIConstants.PRIMARY),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
                }
                // Chỉ xóa thông báo lỗi khi bắt đầu gõ, không phải khi focus
            }
            @Override
            public void focusLost(FocusEvent e) {
                // Chỉ reset về border bình thường nếu không có lỗi
                Boolean hasError = (Boolean) field.getClientProperty("hasError");
                if (hasError == null || !hasError) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(UIConstants.BTN_RADIUS, UIConstants.BORDER),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
                }
            }
        });
    }
    
    private boolean validateFields() {
        boolean valid = true;

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        resetFieldBorder(txtUsername);
        resetFieldBorder(txtPassword);

        if (username.isEmpty() && password.isEmpty()) {
            setErrorBorder(txtUsername);
            setErrorBorder(txtPassword);
            lblError.setText("Vui lòng nhập tên đăng nhập và mật khẩu!");
            valid = false;
        } else if (username.isEmpty()) {
            setErrorBorder(txtUsername);
            // ❌ bỏ txtUsername.requestFocus();
            lblError.setText("Vui lòng nhập tên đăng nhập!");
            valid = false;
        } else if (password.isEmpty()) {
            setErrorBorder(txtPassword);
            // ❌ bỏ txtPassword.requestFocus();
            lblError.setText("Vui lòng nhập mật khẩu!");
            valid = false;
        }

        return valid;
    }

    private void setErrorBorder(JTextField field) {
        field.putClientProperty("hasError", true); // đánh dấu có lỗi
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(UIConstants.BTN_RADIUS, UIConstants.DANGER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private void resetFieldBorder(JTextField field) {
        field.putClientProperty("hasError", false);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(UIConstants.BTN_RADIUS, UIConstants.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private void doLogin() {
        if (!validateFields()) return;

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        btnLogin.setEnabled(false);
        lblError.setText(" ");

        SwingWorker<NhanVien, Void> worker = new SwingWorker<>() {
            @Override protected NhanVien doInBackground() {
                return AuthService.getInstance().dangNhap(username, password);
            }
            @Override protected void done() {
                try {
                    NhanVien nv = get();
                    if (nv != null) {
                        dispose();
                        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
                    } else {
                        lblError.setText("Tên đăng nhập hoặc mật khẩu không đúng!");
                        setErrorBorder(txtUsername);
                        setErrorBorder(txtPassword);
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                        btnLogin.setEnabled(true);
                    }
                } catch (Exception ex) {
                    lblError.setText("Lỗi kết nối: " + ex.getMessage());
                    btnLogin.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
