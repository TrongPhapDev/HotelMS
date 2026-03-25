package UI.panels;

import service.*;
import entity.*;
import UI.MainFrame;
import UI.components.UIConstants;
import UI.components.WrapLayout;
import UI.dialogs.CheckinDialog;
import UI.components.DateTimePicker;
import UI.components.RoundedComponents.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.awt.event.*;


public class DatPhongPanel extends JPanel {

    private final DatPhongService  datPhongService = new DatPhongService();
    private final KhachHangService khService       = new KhachHangService();
    private final PhongService      phongService    = new PhongService();

    private CardLayout   cardLayout;
    private JPanel       cardContainer;

    // ---- Card 1: Danh sách đặt phòng ----
    private DefaultTableModel modelDatPhong;
    private JTable            tableDatPhong;
    private ModernTextField   txtSearchList;

    // ---- Card 2: Tìm & Đặt phòng ----
    private DateTimePicker    txtNgayNhan, txtNgayTra;
    private JComboBox<String> cboTang;
    private JComboBox<String> cboView;
    private JPanel            resultPanel;

    private static final String CARD_LIST   = "LIST";
    private static final String CARD_SEARCH = "SEARCH";

    public DatPhongPanel(MainFrame mainFrame) {
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false);

        cardContainer.add(buildListCard(),   CARD_LIST);
        cardContainer.add(buildSearchCard(), CARD_SEARCH);

        add(cardContainer, BorderLayout.CENTER);
        cardLayout.show(cardContainer, CARD_LIST);
    }

    // ================================================================
    // CARD 1 — DANH SÁCH ĐẶT PHÒNG
    // ================================================================
    private JPanel buildListCard() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Đặt phòng trước");
        title.setFont(UIConstants.FONT_TITLE);
        JLabel sub = new JLabel("Quản lý danh sách phòng đã đặt trước");
        sub.setFont(UIConstants.FONT_BODY);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(sub);

        RoundedButton btnDatPhong = new RoundedButton("+ Đặt phòng", UIConstants.PRIMARY, Color.WHITE);
        btnDatPhong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDatPhong.setPreferredSize(new Dimension(140, 38));
        btnDatPhong.addActionListener(e -> showSearchCard());

        hdr.add(titleBox,    BorderLayout.WEST);
        hdr.add(btnDatPhong, BorderLayout.EAST);

        // Card trắng
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(Color.WHITE);
        card.setShadow(true);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        txtSearchList = new ModernTextField("Tìm khách hàng, số phòng...");
        txtSearchList.setPreferredSize(new Dimension(260, 36));
        txtSearchList.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterListTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterListTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterListTable(); }
        });

        RoundedButton btnHuy = new RoundedButton("✕ Hủy đặt phòng", UIConstants.DANGER, Color.WHITE);
        btnHuy.setEnabled(false);

        toolbar.add(txtSearchList, BorderLayout.WEST);
        toolbar.add(btnHuy,        BorderLayout.EAST);

        // Table
        String[] cols = {"_ma","Mã đặt","Khách hàng","Phòng","Loại phòng","Check-in","Check-out","Số khách","Trạng thái"};
        modelDatPhong = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableDatPhong = new JTable(modelDatPhong);
        styleListTable();
        loadDatPhongTable();

        tableDatPhong.getColumn("_ma").setMaxWidth(0);
        tableDatPhong.getColumn("_ma").setMinWidth(0);
        tableDatPhong.getSelectionModel().addListSelectionListener(
            e -> btnHuy.setEnabled(tableDatPhong.getSelectedRow() >= 0));

        tableDatPhong.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tableDatPhong.getSelectedRow() >= 0) {
                    int row = tableDatPhong.convertRowIndexToModel(tableDatPhong.getSelectedRow());
                    String maDat = (String) modelDatPhong.getValueAt(row, 0);
                    DatPhong dp = datPhongService.getById(maDat);
                    if (dp != null) {
                        showQuickCheckinDialog(dp);
                    } else {
                        JOptionPane.showMessageDialog(DatPhongPanel.this, "Không tìm thấy đặt phòng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnHuy.addActionListener(e -> {
            int row = tableDatPhong.getSelectedRow(); if (row < 0) return;
            String ma  = (String) modelDatPhong.getValueAt(row, 0);
            String ten = (String) modelDatPhong.getValueAt(row, 2);
            int ok = JOptionPane.showConfirmDialog(this,
                "Hủy đặt phòng của \"" + ten + "\"?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                String err = datPhongService.huyDatPhong(ma);
                if (err == null) { loadDatPhongTable(); JOptionPane.showMessageDialog(this, "Đã hủy thành công!"); }
                else JOptionPane.showMessageDialog(this, "Lỗi: " + err, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JScrollPane sp = new JScrollPane(tableDatPhong);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));

        JLabel footer = new JLabel("  Double-click để xem chi tiết");
        footer.setFont(UIConstants.FONT_SMALL);
        footer.setForeground(UIConstants.TEXT_MUTED);
        footer.setBackground(Color.WHITE);
        footer.setOpaque(true);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER),
            BorderFactory.createEmptyBorder(6, 0, 6, 0)));

        card.add(toolbar, BorderLayout.NORTH);
        card.add(sp,      BorderLayout.CENTER);
        card.add(footer,  BorderLayout.SOUTH);

        root.add(hdr,  BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private void filterListTable() {
        String kw = txtSearchList.getText().trim().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelDatPhong);
        tableDatPhong.setRowSorter(sorter);
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return kw.isEmpty()
                    || entry.getStringValue(2).toLowerCase().contains(kw)
                    || entry.getStringValue(3).toLowerCase().contains(kw);
            }
        });
    }

    // ================================================================
    // CARD 2 — TÌM & ĐẶT PHÒNG
    // ================================================================
    private JPanel buildSearchCard() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Tìm phòng trống");
        title.setFont(UIConstants.FONT_TITLE);
        JLabel sub = new JLabel("Chọn ngày và tầng để tìm phòng phù hợp");
        sub.setFont(UIConstants.FONT_BODY);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(sub);

        RoundedButton btnBack = new RoundedButton("← Quay lại", new Color(0xE2E8F0), UIConstants.TEXT_PRIMARY);
        btnBack.setFont(UIConstants.FONT_BODY_BOLD);
        btnBack.setPreferredSize(new Dimension(120, 38));
        btnBack.addActionListener(e -> cardLayout.show(cardContainer, CARD_LIST));

        hdr.add(titleBox, BorderLayout.WEST);
        hdr.add(btnBack,  BorderLayout.EAST);

        // Search box
        RoundedPanel searchCard = new RoundedPanel(UIConstants.CARD_RADIUS);
        searchCard.setBackground(Color.WHITE);
        searchCard.setShadow(true);
        searchCard.setLayout(new BorderLayout());
        searchCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        txtNgayNhan = new DateTimePicker(new Date());
        txtNgayTra  = new DateTimePicker(new Date(System.currentTimeMillis() + 86_400_000L));

        cboTang = new JComboBox<>(new String[]{"Tất cả tầng","Tầng 1","Tầng 2","Tầng 3","Tầng 4","Tầng 5"});
        cboTang.setFont(UIConstants.FONT_BODY);

        java.util.Set<String> views = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        views.add("Tất cả view");
        phongService.getAllPhong().stream()
            .map(Phong::getView)
            .filter(v -> v != null && !v.isBlank())
            .forEach(views::add);
        cboView = new JComboBox<>(views.toArray(new String[0]));
        cboView.setFont(UIConstants.FONT_BODY);

        RoundedButton btnSearch = new RoundedButton("🔍 Tìm phòng", UIConstants.PRIMARY, Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.addActionListener(e -> doSearch());

        // canh đều chiều cao với labeled comp
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        JLabel spacer = new JLabel(" ");
        spacer.setFont(UIConstants.FONT_SMALL_BOLD);
        btnWrap.add(spacer,    BorderLayout.NORTH);
        btnWrap.add(btnSearch, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new GridLayout(1, 5, 14, 0));
        inputRow.setOpaque(false);
        inputRow.add(labeledComp("Ngày nhận phòng *", txtNgayNhan));
        inputRow.add(labeledComp("Ngày trả phòng *",  txtNgayTra));
        inputRow.add(labeledComp("Tầng",              cboTang));
        inputRow.add(labeledComp("View",              cboView));
        inputRow.add(btnWrap);

        searchCard.add(inputRow, BorderLayout.CENTER);

        // Result panel
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setOpaque(false);
        showResultPlaceholder();

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(searchCard,  BorderLayout.NORTH);
        body.add(resultPanel, BorderLayout.CENTER);

        root.add(hdr,  BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JPanel labeledComp(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL_BOLD);
        p.add(lbl,  BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void showResultPlaceholder() {
        resultPanel.removeAll();
        JLabel lbl = new JLabel("Nhập ngày nhận & ngày trả rồi nhấn Tìm phòng", SwingConstants.CENTER);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        resultPanel.add(lbl, BorderLayout.CENTER);
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private void showSearchCard() {
        showResultPlaceholder();
        cardLayout.show(cardContainer, CARD_SEARCH);
    }

    // ================================================================
    // TÌM PHÒNG TRỐNG
    // ================================================================
    private void doSearch() {
        Date checkIn  = txtNgayNhan.getDate();
        Date checkOut = txtNgayTra.getDate();
        if (!checkOut.after(checkIn)) {
            JOptionPane.showMessageDialog(this, "Ngày trả phải sau ngày nhận!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tangSel    = (String) cboTang.getSelectedItem();
        String tangFilter = (tangSel != null && !tangSel.equals("Tất cả tầng"))
            ? tangSel.replace("Tầng ", "").trim() : null;
        String viewSel    = (String) cboView.getSelectedItem();
        String viewFilter = (viewSel != null && !viewSel.equals("Tất cả view"))
            ? viewSel : null;

        java.util.List<Phong> allRooms = datPhongService.timPhongTrong(checkIn, checkOut, 1);
        java.util.List<Phong> rooms    = new ArrayList<>();
        for (Phong ph : allRooms) {
            boolean matchTang = tangFilter == null || String.valueOf(ph.getTang()).equals(tangFilter);
            String roomView = ph.getView();
            boolean matchView = viewFilter == null || roomView.equalsIgnoreCase(viewFilter);
            if (matchTang && matchView) rooms.add(ph);
        }
        showResults(rooms, checkIn, checkOut);
    }

    private void showResults(java.util.List<Phong> rooms, Date checkIn, Date checkOut) {
        resultPanel.removeAll();
        if (rooms.isEmpty()) {
            JLabel lbl = new JLabel("Không tìm thấy phòng trống phù hợp!", SwingConstants.CENTER);
            lbl.setFont(UIConstants.FONT_BODY);
            lbl.setForeground(UIConstants.DANGER);
            resultPanel.add(lbl, BorderLayout.CENTER);
        } else {
            JLabel cnt = new JLabel("Tìm thấy " + rooms.size() + " phòng trống");
            cnt.setFont(UIConstants.FONT_BODY_BOLD);
            cnt.setBorder(BorderFactory.createEmptyBorder(10, 0, 8, 0));

            JPanel grid = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 10));
            grid.setOpaque(false);
            long soNgay = new ThuePhongService().tinhSoNgay(checkIn, checkOut);
            for (Phong ph : rooms) grid.add(buildRoomCard(ph, soNgay, checkIn, checkOut));

            JScrollPane sp = new JScrollPane(grid);
            sp.setOpaque(false); sp.getViewport().setOpaque(false); sp.setBorder(null);

            resultPanel.add(cnt, BorderLayout.NORTH);
            resultPanel.add(sp,  BorderLayout.CENTER);
        }
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private RoundedPanel buildRoomCard(Phong p, long soNgay, Date checkIn, Date checkOut) {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(Color.WHITE); card.setShadow(true);
        card.setLayout(new BorderLayout(0, 6));
        card.setPreferredSize(new Dimension(220, 170));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel lRoom  = new JLabel("P." + p.getSoPhong()); lRoom.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel lType  = new JLabel(p.getTenLoaiPhong() != null ? p.getTenLoaiPhong() : "");
        lType.setFont(UIConstants.FONT_SMALL); lType.setForeground(UIConstants.TEXT_SECONDARY);
        JLabel lFloor = new JLabel("Tầng " + p.getTang() + " · " + p.getSucChua() + " người");
        lFloor.setFont(UIConstants.FONT_SMALL); lFloor.setForeground(UIConstants.TEXT_MUTED);
        JLabel lPrice = new JLabel(String.format("%,.0fđ / đêm", (double) p.getGiaTheoNgay()));
        lPrice.setFont(UIConstants.FONT_BODY_BOLD); lPrice.setForeground(UIConstants.SUCCESS);
        JLabel lTotal = new JLabel(String.format("= %,.0fđ (%d đêm)", (double) p.getGiaTheoNgay() * soNgay, soNgay));
        lTotal.setFont(UIConstants.FONT_SMALL); lTotal.setForeground(UIConstants.TEXT_MUTED);

        RoundedButton btnDat = new RoundedButton("Đặt phòng này", UIConstants.PRIMARY, Color.WHITE);
        btnDat.addActionListener(e -> showDatPhongDialog(p, checkIn, checkOut));

        JPanel info = new JPanel(); info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(lRoom); info.add(Box.createVerticalStrut(2));
        info.add(lType); info.add(lFloor);
        info.add(Box.createVerticalStrut(6));
        info.add(lPrice); info.add(lTotal);

        card.add(info,   BorderLayout.CENTER);
        card.add(btnDat, BorderLayout.SOUTH);
        return card;
    }

    // ================================================================
    // DIALOG ĐẶT PHÒNG — nhập họ tên + SĐT
    // ================================================================
    private void showDatPhongDialog(Phong p, Date checkIn, Date checkOut) {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Đặt phòng P." + p.getSoPhong(), true);
        dlg.setSize(460, 440);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        JLabel hdr = new JLabel("Xác nhận đặt phòng P." + p.getSoPhong());
        hdr.setFont(UIConstants.FONT_HEADER);
        hdr.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(hdr);
        panel.add(Box.createVerticalStrut(14));

        for (String[] row : new String[][]{
            {"Loại phòng:", p.getTenLoaiPhong() != null ? p.getTenLoaiPhong() : ""},
            {"Tầng:",       "Tầng " + p.getTang()},
            {"Check-in:",   sdf.format(checkIn)},
            {"Check-out:",  sdf.format(checkOut)},
            {"Giá/đêm:",    String.format("%,.0fđ", (double) p.getGiaTheoNgay())}
        }) {
            JPanel r = new JPanel(new BorderLayout(8, 0));
            r.setOpaque(false);
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
            r.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel k = new JLabel(row[0]); k.setFont(UIConstants.FONT_SMALL_BOLD); k.setPreferredSize(new Dimension(100, 20));
            JLabel v = new JLabel(row[1]); v.setFont(UIConstants.FONT_BODY);
            r.add(k, BorderLayout.WEST); r.add(v, BorderLayout.CENTER);
            panel.add(r); panel.add(Box.createVerticalStrut(3));
        }

        panel.add(Box.createVerticalStrut(12));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createVerticalStrut(12));

        // Họ tên
        JLabel lHoTen = new JLabel("Họ và tên khách hàng *");
        lHoTen.setFont(UIConstants.FONT_SMALL_BOLD); lHoTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        ModernTextField txtHoTen = new ModernTextField("Nhập họ và tên...");
        txtHoTen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtHoTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lHoTen); panel.add(Box.createVerticalStrut(4)); panel.add(txtHoTen);
        panel.add(Box.createVerticalStrut(10));

        // Số điện thoại
        JLabel lSDT = new JLabel("Số điện thoại *");
        lSDT.setFont(UIConstants.FONT_SMALL_BOLD); lSDT.setAlignmentX(Component.LEFT_ALIGNMENT);
        ModernTextField txtSDT = new ModernTextField("Nhập số điện thoại...");
        txtSDT.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtSDT.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lSDT); panel.add(Box.createVerticalStrut(4)); panel.add(txtSDT);
        panel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false); btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        RoundedButton btnCancel = new RoundedButton("Huỷ", new Color(0xE2E8F0), UIConstants.TEXT_PRIMARY);
        RoundedButton btnOk     = new RoundedButton("✓ Xác nhận đặt phòng", UIConstants.PRIMARY, Color.WHITE);

        btnCancel.addActionListener(e -> dlg.dispose());

        btnOk.addActionListener(e -> {
            String hoTen = txtHoTen.getText().trim();
            String sdt   = txtSDT.getText().trim();

            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập họ và tên khách hàng!"); return;
            }
            if (sdt.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập số điện thoại!"); return;
            }
            if (!sdt.matches("\\d{9,11}")) {
                JOptionPane.showMessageDialog(dlg, "Số điện thoại không hợp lệ (9-11 chữ số)!"); return;
            }

            // Tìm hoặc tạo khách hàng theo SĐT
            KhachHang kh = khService.getByPhone(sdt);
            if (kh == null) {
                kh = new KhachHang();
                kh.setHoTen(hoTen);
                kh.setSoDienThoai(sdt);
                kh.setCccd("");
                String errKH = khService.them(kh);
                if (errKH != null) {
                    JOptionPane.showMessageDialog(dlg, "Lỗi tạo khách hàng: " + errKH, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                kh = khService.getByPhone(sdt);
            }

            DatPhong dp = new DatPhong();
            dp.setMaKH(kh.getMaKH());
            dp.setSoPhong(p.getSoPhong());
            dp.setNgayNhanDK(checkIn);
            dp.setNgayTraDK(checkOut);
            dp.setSoKhach(1);
            dp.setTrangThai("Đã xác nhận");
            dp.setMaNV(AuthService.getInstance().getCurrentMaNV());

            String err = datPhongService.datPhong(dp);
            if (err == null) {
                JOptionPane.showMessageDialog(dlg, "✓ Đặt phòng thành công!\nMã đặt: " + dp.getMaDatPhong());
                dlg.dispose();
                refresh();
                cardLayout.show(cardContainer, CARD_LIST);
            } else {
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + err, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btns.add(btnCancel); btns.add(btnOk);
        panel.add(btns);

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    // ================================================================
    // TABLE STYLE & LOAD
    // ================================================================
    private void styleListTable() {
        tableDatPhong.setFont(UIConstants.FONT_BODY); tableDatPhong.setRowHeight(40);
        tableDatPhong.setShowGrid(false); tableDatPhong.setIntercellSpacing(new Dimension(0, 0));
        tableDatPhong.setBackground(Color.WHITE); tableDatPhong.setSelectionBackground(UIConstants.PRIMARY_LIGHT);
        tableDatPhong.getTableHeader().setFont(UIConstants.FONT_SMALL_BOLD);
        tableDatPhong.getTableHeader().setBackground(UIConstants.BG_TABLE_HEADER);
        tableDatPhong.getTableHeader().setForeground(UIConstants.TEXT_SECONDARY);
        tableDatPhong.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));
        tableDatPhong.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                lbl.setBackground(sel ? UIConstants.PRIMARY_LIGHT
                    : (row % 2 == 0 ? Color.WHITE : new Color(0xFAFAFA)));
                lbl.setFont(UIConstants.FONT_BODY); lbl.setForeground(UIConstants.TEXT_PRIMARY);
                String val = v != null ? v.toString() : "";
                if (col == 8) {
                    Color c;
                    if      ("Đã xác nhận".equals(val))  c = UIConstants.SUCCESS;
                    else if ("Chờ xác nhận".equals(val)) c = UIConstants.WARNING;
                    else if ("Đã hủy".equals(val))       c = UIConstants.DANGER;
                    else                                  c = UIConstants.TEXT_MUTED;
                    lbl.setForeground(c); lbl.setFont(UIConstants.FONT_SMALL_BOLD);
                }
                return lbl;
            }
        });
    }

    private void loadDatPhongTable() {
        modelDatPhong.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (DatPhong dp : datPhongService.getAll()) {
            modelDatPhong.addRow(new Object[]{
                dp.getMaDatPhong(), dp.getMaDatPhong(),
                dp.getTenKhachHang() != null ? dp.getTenKhachHang() : "—",
                dp.getSoPhong(),
                dp.getTenLoaiPhong() != null ? dp.getTenLoaiPhong() : "",
                dp.getNgayNhanDK() != null ? sdf.format(dp.getNgayNhanDK()) : "",
                dp.getNgayTraDK()  != null ? sdf.format(dp.getNgayTraDK())  : "",
                dp.getSoKhach(), dp.getTrangThai()
            });
        }
    }

    private void showQuickCheckinDialog(DatPhong dp) {
        if (dp == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Check-in đặt phòng " + dp.getMaDatPhong(), true);
        dlg.setSize(420, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        JLabel lblNow = new JLabel("Thời gian hiện tại: " + sdf.format(new Date()));
        lblNow.setFont(UIConstants.FONT_SMALL_BOLD);

        JLabel lblInfo = new JLabel("Phòng: " + dp.getSoPhong() + " | Khách: " + (dp.getTenKhachHang() != null ? dp.getTenKhachHang() : "—") );
        lblInfo.setFont(UIConstants.FONT_BODY);

        JLabel lblExpected = new JLabel("Dự kiến: " + (dp.getNgayNhanDK() != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dp.getNgayNhanDK()) : "-")
            + " → " + (dp.getNgayTraDK() != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dp.getNgayTraDK()) : "-"));
        lblExpected.setFont(UIConstants.FONT_BODY);

        JPanel lines = new JPanel(); lines.setOpaque(false); lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
        lines.add(lblNow); lines.add(Box.createVerticalStrut(6)); lines.add(lblInfo); lines.add(Box.createVerticalStrut(4)); lines.add(lblExpected);

        container.add(lines, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0xE2E8F0), UIConstants.TEXT_PRIMARY);
        RoundedButton btnCheckin = new RoundedButton("Checkin", UIConstants.PRIMARY, Color.WHITE);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnCheckin.addActionListener(e -> {
            Phong phong = phongService.getPhongById(dp.getSoPhong());
            if (phong == null) {
                JOptionPane.showMessageDialog(dlg, "Không tìm thấy thông tin phòng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String status = phong.getTrangThai();
            if (status == null) status = "Không xác định";
            if (!"Có sẵn".equals(status) && !"Đã đặt".equals(status)) {
                JOptionPane.showMessageDialog(dlg, "Phòng hiện không trống. Trạng thái: " + status, "Không thể checkin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            KhachHang kh = null;
            if (dp.getMaKH() != null) {
                kh = khService.getById(dp.getMaKH());
            }

            dlg.dispose();
            CheckinDialog ck = new CheckinDialog((Frame) SwingUtilities.getWindowAncestor(this), phong, kh, dp);
            ck.setVisible(true);
            if (ck.isConfirmed()) {
                refresh();
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnCheckin);

        container.add(btnPanel, BorderLayout.SOUTH);
        dlg.setContentPane(container);
        dlg.setVisible(true);
    }

    public void refresh() {
        loadDatPhongTable();
    }
}