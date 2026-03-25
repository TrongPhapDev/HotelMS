package UI;

import UI.panels.*;
import entity.NhanVien;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import service.AuthService;
import UI.components.RoundedComponents.ModernTextField;
import UI.components.UIConstants;

public class MainFrame extends JFrame {

    private JPanel contentArea;
    private CardLayout cardLayout;
    private JLabel lblCurrentTime;
    private String activeMenu = "tongquan";

    private TongQuanPanel pTongQuan;
    private ThuePhongPanel pThuePhong;
    private KhachHangPanel pKhachHang;
    private QLHeThongPanel pQLHeThong;
    private ThongKePanel pThongKe;
    private NhanVienPanel pNhanVien;
    private DatPhongPanel pDatPhong;

    public MainFrame() {
        setTitle("Hotel MS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 820);
        setLocationRelativeTo(null);
        initUI();
        startClock();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 246, 250));

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(buildSidebar(), BorderLayout.WEST);
        body.add(buildContent(), BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ===== HEADER (WEB ADMIN STYLE) =====
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
            }
        };

        header.setBackground(new Color(0x1E2337));
        header.setPreferredSize(new Dimension(0, 60));

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER));
        center.setOpaque(false);

        ModernTextField txtSearch = new ModernTextField("Tìm kiếm...");
        txtSearch.setPreferredSize(new Dimension(320, 36));
        center.add(txtSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        right.setOpaque(false);

        lblCurrentTime = new JLabel();
        lblCurrentTime.setForeground(new Color(0xA0A8C0));

        NhanVien user = AuthService.getInstance().getCurrentUser();
        String initials = user != null ? user.getHoTen().substring(0, 1).toUpperCase() : "A";

        JPanel avatar = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(67, 97, 238));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (getWidth()-fm.stringWidth(initials))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        avatar.setPreferredSize(new Dimension(36,36));

        JLabel lblName = new JLabel(user != null ? user.getHoTen() : "Admin");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(new Color(0x2D3452));
        btnLogout.setForeground(new Color(0xFF6B6B));
        btnLogout.setBorderPainted(false);
        btnLogout.addActionListener(e -> doLogout());

        right.add(lblCurrentTime);
        right.add(avatar);
        right.add(lblName);
        right.add(btnLogout);

        header.add(center, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ===== SIDEBAR =====
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0x2D3452));
                g.fillRect(getWidth() - 1, 0, 1, getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(0x1E2337)); // thay bằng màu tương ứng
        sidebar.setLayout(new BorderLayout());

        // ── Logo
        

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        JLabel hotelName = new JLabel("Hotel MS");
        hotelName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        hotelName.setForeground(Color.WHITE); // thay bằng màu tương ứng
        JLabel sub = new JLabel("Enterprise Admin");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(0x8892B0)); // thay bằng màu tương ứng
        textBlock.add(hotelName);
        textBlock.add(sub);

 

        // ── Menu items
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        String[][] menus = {
            {"tongquan",   "Tổng quan"},
            {"thuephong",  "Thuê/Trả phòng"},
            {"khachhang",  "Khách hàng"},
            {"qlhethong",  "Quản lý hệ thống"},
            {"thongke",    "Thống kê"},
            {"nhanvien",   "Quản lý tài khoản"},
            {"datphong",   "Đặt phòng"},
        };

        for (String[] m : menus) {
            JPanel item = buildMenuItem(m[0], m[1]);
            menu.add(item);
            menu.add(Box.createVerticalStrut(2));
        }

        // ── Settings at bottom
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        bottom.setOpaque(false);
        JLabel settings = new JLabel("⚙  Cài đặt");
        settings.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        settings.setForeground(new Color(0x8892B0)); // thay bằng màu tương ứng
        settings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Gắn action nếu cần:
        // settings.addMouseListener(new MouseAdapter() {
        //     public void mouseClicked(MouseEvent e) { showPage("caidat"); }
        // });
        bottom.add(settings);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
    
        topSection.add(menu,      BorderLayout.CENTER);

        sidebar.add(topSection, BorderLayout.CENTER);
        sidebar.add(bottom,     BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildMenuItem(String key, String label) {
        JPanel item = new JPanel(new BorderLayout()) {
            boolean hover = false;

            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (key.equals(activeMenu)) {
                    g2.setColor(new Color(0x2D3452));   // nền active tối hơn
                    g2.fillRoundRect(5,2,getWidth()-10,getHeight()-4,12,12);
                    g2.setColor(new Color(0x4361EE));   // thanh xanh bên trái
                    g2.fillRoundRect(5,2,4,getHeight()-4,8,8);
                } else if (hover) {
                    g2.setColor(new Color(0x252A40));   // hover nhạt hơn
                    g2.fillRoundRect(5,2,getWidth()-10,getHeight()-4,12,12);
                }
            }
        };

        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        item.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(key.equals(activeMenu) ? Color.WHITE : new Color(0xA0A8C0));
        item.add(lbl, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { item.repaint(); }
            public void mouseExited(MouseEvent e) { item.repaint(); }
            public void mouseClicked(MouseEvent e) { navigateTo(key); }
        });

        return item;
    }
    
    

    // ===== CONTENT (GIỮ NGUYÊN) =====
    private JPanel buildContent() {
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);

        pTongQuan = new TongQuanPanel(this);
        pThuePhong = new ThuePhongPanel(this);
        pKhachHang = new KhachHangPanel(this);
        pQLHeThong = new QLHeThongPanel(this);
        pThongKe = new ThongKePanel(this);
        pNhanVien = new NhanVienPanel(this);
        pDatPhong = new DatPhongPanel(this);

        contentArea.add(pTongQuan, "tongquan");
        contentArea.add(pThuePhong, "thuephong");
        contentArea.add(pKhachHang, "khachhang");
        contentArea.add(pQLHeThong, "qlhethong");
        contentArea.add(pThongKe, "thongke");
        contentArea.add(pNhanVien, "nhanvien");
        contentArea.add(pDatPhong, "datphong");

        return contentArea;
    }

    public void navigateTo(String key) {
        activeMenu = key;
        cardLayout.show(contentArea, key);

        if ("tongquan".equals(key)) pTongQuan.refresh();
        else if ("thuephong".equals(key)) pThuePhong.refresh();
        else if ("khachhang".equals(key)) pKhachHang.refresh();
        else if ("qlhethong".equals(key)) pQLHeThong.refresh();
        else if ("thongke".equals(key)) pThongKe.refresh();
        else if ("nhanvien".equals(key)) pNhanVien.refresh();
        else if ("datphong".equals(key)) pDatPhong.refresh();

        repaint();
    }

    private void startClock() {
        new Timer(1000, e -> {
            String time = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date());
            lblCurrentTime.setText(time);
        }).start();
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.getInstance().dangXuat();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}