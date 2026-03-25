package UI.panels;

import UI.MainFrame;
import UI.components.RoundedComponents.*;
import UI.components.RoundedComponents.RoundedBorder;
import UI.components.RoundedComponents.RoundedButton;
import UI.components.RoundedComponents.RoundedPanel;
import UI.components.UIConstants;
import UI.components.WrapLayout;
import UI.dialogs.*;
import entity.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import service.*;

public class ThuePhongPanel extends JPanel {

    private final MainFrame        mainFrame;
    private final PhongService     phongService    = new PhongService();
    private final ThuePhongService thuePhongService = new ThuePhongService();

    private JPanel  roomGrid;
    private String  currentFilter = "Tất cả";
    private String  currentView   = "Tất cả";
    private JPanel  filterTabsPanel;

    public ThuePhongPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

        main.add(buildHeader(), BorderLayout.NORTH);

        // Filter + Scroll container
        JPanel filterAndScroll = new JPanel(new BorderLayout(0, 12));
        filterAndScroll.setOpaque(false);
        filterAndScroll.add(buildFilterTabs(), BorderLayout.NORTH);

        // Room grid inside scroll pane
        roomGrid = new JPanel();
        roomGrid.setOpaque(false);
        refreshGrid();

        JScrollPane scroll = new JScrollPane(roomGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        filterAndScroll.add(scroll, BorderLayout.CENTER);
        main.add(filterAndScroll, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(main, BorderLayout.CENTER);
    }

    // ---- Header ----
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Sơ đồ phòng – Thuê phòng");
        title.setFont(UIConstants.FONT_TITLE);
        JLabel sub = new JLabel("Click vào phòng để nhận phòng, xem chi tiết hoặc trả phòng");
        sub.setFont(UIConstants.FONT_BODY);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        RoundedButton btnDatPhong = new RoundedButton("→ Đặt phòng", new Color(0xFF8A00), Color.WHITE);
        btnDatPhong.setFont(UIConstants.FONT_BODY_BOLD);
        btnDatPhong.addActionListener(e -> mainFrame.navigateTo("datphong"));

        RoundedButton btnCheckin = new RoundedButton("+ Nhận phòng mới", UIConstants.PRIMARY, Color.WHITE);
        btnCheckin.addActionListener(e -> showCheckinDialog(null));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnDatPhong);
        right.add(btnCheckin);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // ---- Filter tabs ----
    private JPanel buildFilterTabs() {
        filterTabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterTabsPanel.setOpaque(false);

        // Status filter
        JLabel lblFilter = new JLabel("Lọc trạng thái:");
        lblFilter.setFont(UIConstants.FONT_BODY);
        lblFilter.setForeground(UIConstants.TEXT_PRIMARY);
        filterTabsPanel.add(lblFilter);

        String[] statusOptions = {"Tất cả", "Trống", "Đang ở", "Chờ dọn", "Đã đặt", "Bảo trì"};
        JComboBox<String> cboStatus = new JComboBox<>(statusOptions);
        cboStatus.setFont(UIConstants.FONT_BODY);
        cboStatus.setSelectedItem(currentFilter);
        cboStatus.setPreferredSize(new Dimension(150, 34));
        styleCombo(cboStatus);
        cboStatus.addActionListener(e -> {
            currentFilter = (String) cboStatus.getSelectedItem();
            refreshGrid();
        });
        filterTabsPanel.add(cboStatus);

        // View filter
        JLabel lblView = new JLabel("Lọc theo view:");
        lblView.setFont(UIConstants.FONT_BODY);
        lblView.setForeground(UIConstants.TEXT_PRIMARY);
        filterTabsPanel.add(lblView);

        java.util.Set<String> views = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        views.add("Tất cả");
        for (Phong p : phongService.getAllPhong()) {
            String v = p.getView();
            if (v != null && !v.isBlank()) views.add(v);
        }
        JComboBox<String> cboView = new JComboBox<>(views.toArray(new String[0]));
        cboView.setFont(UIConstants.FONT_BODY);
        cboView.setSelectedItem(currentView != null ? currentView : "Tất cả");
        cboView.setPreferredSize(new Dimension(150, 34));
        styleCombo(cboView);
        cboView.addActionListener(e -> {
            currentView = (String) cboView.getSelectedItem();
            refreshGrid();
        });
        filterTabsPanel.add(cboView);

        return filterTabsPanel;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(new Color(245, 248, 255));
        combo.setForeground(new Color(20, 35, 80));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 195, 230), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        combo.setOpaque(true);
        combo.setFocusable(false);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                label.setForeground(new Color(20, 35, 80));
                return label;
            }
        });
    }

    private void styleFilterBtn(JToggleButton btn, boolean active) {
        if (active) {
            btn.setBackground(UIConstants.PRIMARY); btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, UIConstants.PRIMARY),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        } else {
            btn.setBackground(Color.WHITE); btn.setForeground(UIConstants.TEXT_SECONDARY);
            btn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        }
    }

    // ---- Room grid ----
    private void refreshGrid() {
        roomGrid.removeAll();
        roomGrid.setLayout(new BoxLayout(roomGrid, BoxLayout.Y_AXIS));

        java.util.List<Phong> allPhong = phongService.getAllPhong();
        System.out.println("🔍 ThuePhongPanel.refreshGrid() - Loaded " + allPhong.size() + " phòng");

        // Group by floor, apply filter
        Map<Integer, java.util.List<Phong>> byFloor = new TreeMap<>();
        for (Phong p : allPhong) {
            boolean show;
            String tt = p.getTrangThai() != null ? p.getTrangThai() : "";
            if      ("Trống".equals(currentFilter))    show = "Có sẵn".equals(tt);
            else if ("Đang ở".equals(currentFilter))   show = "Đang thuê".equals(tt);
            else if ("Chờ dọn".equals(currentFilter))  show = "Vệ sinh".equals(tt);
            else if ("Đã đặt".equals(currentFilter))   show = "Đã đặt".equals(tt);
            else if ("Bảo trì".equals(currentFilter))  show = "Bảo trì".equals(tt);
            else                                        show = true;

            String view = p.getView();
            boolean viewMatch = "Tất cả".equals(currentView) || view.equalsIgnoreCase(currentView);

            if (show && viewMatch) byFloor.computeIfAbsent(p.getTang(), k -> new ArrayList<>()).add(p);
        }
        int filteredCount = byFloor.values().stream().mapToInt(l -> l.size()).sum();
        System.out.println("📊 After filter '" + currentFilter + "': " + filteredCount + " phòng");

        if (byFloor.isEmpty()) {
            JLabel lbl = new JLabel("Không có phòng nào phù hợp", SwingConstants.CENTER);
            lbl.setFont(UIConstants.FONT_BODY); lbl.setForeground(UIConstants.TEXT_MUTED);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            roomGrid.add(Box.createVerticalStrut(40));
            roomGrid.add(lbl);
        } else {
            for (Map.Entry<Integer, java.util.List<Phong>> entry : byFloor.entrySet()) {
                roomGrid.add(buildFloorSection(entry.getKey(), entry.getValue()));
                roomGrid.add(Box.createVerticalStrut(12));
            }
        }

        roomGrid.revalidate();
        roomGrid.repaint();
    }

    // ---- Floor section ----
    private JPanel buildFloorSection(int floor, java.util.List<Phong> phongs) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));

        // Floor label
        JPanel floorHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        floorHeader.setOpaque(false);
        floorHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        floorHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel lblFloor = new JLabel("Tầng " + floor);
        lblFloor.setFont(UIConstants.FONT_HEADER);
        lblFloor.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel lblCount = new JLabel(phongs.size() + " phòng");
        lblCount.setFont(UIConstants.FONT_SMALL);
        lblCount.setForeground(UIConstants.TEXT_MUTED);
        floorHeader.add(lblFloor);
        floorHeader.add(lblCount);

        // Separator (sáng hơn, nổi bật nhẹ)
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(new Color(200, 210, 245));
        separator.setBackground(new Color(235, 240, 255));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Cards
        JPanel cardsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 8));
        cardsPanel.setOpaque(false);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9999));
        for (Phong p : phongs) cardsPanel.add(buildRoomCard(p));

        section.add(floorHeader);
        section.add(Box.createVerticalStrut(8));
        section.add(separator);
        section.add(Box.createVerticalStrut(8));
        section.add(cardsPanel);
        return section;
    }

    // ---- Room card ----
    private RoundedPanel buildRoomCard(Phong p) {
        String tt         = p.getTrangThai() != null ? p.getTrangThai() : "Có sẵn";
        Color  borderClr  = UIConstants.getTrangThaiPhongColor(tt);
        Color  bgHover    = UIConstants.getTrangThaiPhongBg(tt);

        RoundedPanel card = new RoundedPanel(10);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(0, 4));
        card.setPreferredSize(new Dimension(196, 128));
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, borderClr),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Top row: số phòng + trạng thái
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel lblNum = new JLabel(p.getSoPhong());
        lblNum.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JLabel lblTT  = new JLabel(tt);
        lblTT.setFont(UIConstants.FONT_SMALL_BOLD);
        lblTT.setForeground(borderClr);
        topRow.add(lblNum, BorderLayout.WEST);
        topRow.add(lblTT,  BorderLayout.EAST);

        // Middle: loại + mô tả
        JLabel lblType = new JLabel(p.getTenLoaiPhong() != null ? p.getTenLoaiPhong() : "");
        lblType.setFont(UIConstants.FONT_SMALL);
        lblType.setForeground(UIConstants.TEXT_SECONDARY);

        String viewText = p.getView();
        JLabel lblView = new JLabel("View: " + viewText);
        lblView.setFont(UIConstants.FONT_TINY);
        lblView.setForeground(UIConstants.TEXT_MUTED);

        // Bottom: giá + khách
        JPanel botRow = new JPanel(new BorderLayout());
        botRow.setOpaque(false);
        JLabel lblPrice = new JLabel(String.format("%,.0fđ/đêm", (double) p.getGiaTheoNgay()));
        lblPrice.setFont(UIConstants.FONT_SMALL_BOLD);
        lblPrice.setForeground(UIConstants.SUCCESS);

        String guestTxt = p.getTenKhachHienTai() != null
            ? "▶ " + truncate(p.getTenKhachHienTai(), 16)
            : p.getSucChua() + " người tối đa";
        JLabel lblGuest = new JLabel(guestTxt);
        lblGuest.setFont(UIConstants.FONT_SMALL);
        lblGuest.setForeground(p.getTenKhachHienTai() != null ? UIConstants.PRIMARY : UIConstants.TEXT_MUTED);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(lblType);
        center.add(Box.createVerticalStrut(2));
        center.add(lblView);
        center.add(Box.createVerticalStrut(4));
        center.add(lblGuest);

        card.add(topRow, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(lblPrice, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleRoomClick(p); }
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(bgHover); card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { card.setBackground(Color.WHITE); card.repaint(); }
        });
        return card;
    }

    // ---- Click handler ----
    private void handleRoomClick(Phong p) {
        String tt = p.getTrangThai() != null ? p.getTrangThai() : "";
        if ("Có sẵn".equals(tt)) {
            showCheckinDialog(p);
        } else if ("Đang thuê".equals(tt)) {
            showCheckoutDialog(p);
        } else if ("Đã đặt".equals(tt)) {
            int opt = JOptionPane.showConfirmDialog(mainFrame,
                "Phòng " + p.getSoPhong() + " đã đặt trước.\nNhận phòng ngay?",
                "Nhận phòng", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) showCheckinDialog(p);
        } else if ("Vệ sinh".equals(tt)) {
            int opt = JOptionPane.showConfirmDialog(mainFrame,
                "Đánh dấu phòng " + p.getSoPhong() + " sẵn sàng?",
                "Cập nhật", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                phongService.updateTrangThai(p.getSoPhong(), "Có sẵn");
                refresh();
            }
        } else {
            JOptionPane.showMessageDialog(mainFrame,
                "Phòng " + p.getSoPhong() + " - Trạng thái: " + p.getTrangThai(),
                "Thông tin", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showCheckinDialog(Phong p) {
        CheckinDialog dlg = new CheckinDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this), p);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) refresh();
    }

    private void showCheckoutDialog(Phong p) {
        ThuePhong tp = thuePhongService.getActiveByPhong(p.getSoPhong());
        if (tp == null) {
            JOptionPane.showMessageDialog(mainFrame, "Không tìm thấy thông tin thuê phòng!");
            return;
        }
        HoaDonDialog dlg = new HoaDonDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this), tp);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) refresh();
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "…" : s;
    }

    public void refresh() {
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }
}

