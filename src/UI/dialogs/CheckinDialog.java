package UI.dialogs;

import service.AuthService;
import service.DatPhongService;

import UI.components.DateTimePicker;
import service.KhachHangService;
import service.ThuePhongService;
import service.DichVuService;
import service.PhongService;
import entity.*;
import UI.components.UIConstants;
import UI.components.RoundedComponents.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

public class CheckinDialog extends JDialog {

    private final ThuePhongService  thuePhongService = new ThuePhongService();
    private final DichVuService     dichVuService    = new DichVuService();
    private final KhachHangService  khService        = new KhachHangService();
    private final PhongService      phongService     = new PhongService();
    private final DatPhongService   datPhongService  = new DatPhongService();

    private Phong      selectedPhong;
    private KhachHang  selectedKhach;
    private DatPhong   selectedDatPhong;
    private JComboBox<Phong> cboPhong;
    private JLabel     lblInfo;

    private int     currentStep = 1;
    private static final int TOTAL_STEPS = 5;
    private JPanel  stepContent;
    private JLabel  lblStepInfo;
    private boolean confirmed = false;

    // Step inputs
    private JTextField    txtTenKH, txtSDT, txtCCCD, txtEmail;
    private DateTimePicker pickerNgayNhan, pickerNgayTra;
    private JSpinner      spnSoKhach;
    private JComboBox<String> cboDV;
    private JSpinner      spnSLDV;
    private JPanel        dvListPanel;
    private JLabel        lblTongDV;
    private java.util.List<DichVu>  allDichVu = new ArrayList<>();
    private final java.util.List<DichVuThue> selectedDV = new ArrayList<>();

    // Bottom bar buttons (need reference to update label)
    private RoundedButton btnBack, btnNext;

    public CheckinDialog(Frame parent, Phong phong) {
        super(parent,
            "Nhận phòng (Check-in)" + (phong != null ? " – P." + phong.getSoPhong() : ""),
            true);
        this.selectedPhong = phong;
        setSize(660, 620);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        buildUI();
    }

    public CheckinDialog(Frame parent, Phong phong, KhachHang kh, DatPhong dp) {
        this(parent, phong);
        this.selectedKhach = kh;
        this.selectedDatPhong = dp;

        if (kh != null) {
            if (txtTenKH != null) txtTenKH.setText(kh.getHoTen());
            if (txtSDT != null) txtSDT.setText(kh.getSoDienThoai());
            if (txtCCCD != null) txtCCCD.setText(kh.getCccd());
            if (txtEmail != null) txtEmail.setText(kh.getEmail() != null ? kh.getEmail() : "");
        }
        if (dp != null) {
            if (pickerNgayNhan != null) pickerNgayNhan.setDate(dp.getNgayNhanDK() != null ? dp.getNgayNhanDK() : new Date());
            if (pickerNgayTra != null) pickerNgayTra.setDate(dp.getNgayTraDK() != null ? dp.getNgayTraDK() : new Date(System.currentTimeMillis() + 86_400_000L));
            if (spnSoKhach != null) spnSoKhach.setValue(dp.getSoKhach() > 0 ? dp.getSoKhach() : 1);
        }
    }

    // =====================================================================
    // BUILD UI
    // =====================================================================
    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildTopBar(),    BorderLayout.NORTH);
        stepContent = new JPanel(new CardLayout());
        stepContent.setBackground(Color.WHITE);
        stepContent.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));
        stepContent.add(buildStep1(), "1");
        stepContent.add(buildStep2(), "2");
        stepContent.add(buildStep3(), "3");
        stepContent.add(buildStep4(), "4");
        stepContent.add(buildStep5(), "5");
        add(stepContent,      BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);
        showStep(1);
    }

    // ---- Top bar: title + stepper ----
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(20, 32, 4, 32));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Nhận phòng – Check-in");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        String info = selectedPhong != null
            ? "P." + selectedPhong.getSoPhong() + " · " + selectedPhong.getTenLoaiPhong()
              + " · " + String.format("%,.0fđ/đêm", (double) selectedPhong.getGiaTheoNgay())
            : "Chọn phòng để bắt đầu";
        lblInfo = new JLabel(info);
        lblInfo.setFont(UIConstants.FONT_BODY);
        lblInfo.setForeground(UIConstants.TEXT_SECONDARY);
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblInfo);
        top.add(titlePanel, BorderLayout.WEST);

        // Stepper
        top.add(buildStepper(), BorderLayout.SOUTH);
        return top;
    }

    private JPanel buildStepper() {
        String[] names = {"Phòng", "Khách hàng", "Ngày & Giờ", "Dịch vụ", "Xác nhận"};
        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        stepper.setOpaque(false);

        for (int i = 0; i < TOTAL_STEPS; i++) {
            final int step = i + 1;

            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean done   = step < currentStep;
                    boolean active = step == currentStep;
                    Color bg = (done || active) ? UIConstants.PRIMARY : new Color(0xCBD5E1);
                    g2.setColor(bg);
                    g2.fillOval(0, 0, 28, 28);
                    g2.setColor(Color.WHITE);
                    g2.setFont(UIConstants.FONT_SMALL_BOLD);
                    String t = done ? "✓" : String.valueOf(step);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(t, (28 - fm.stringWidth(t)) / 2, (28 + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            dot.setOpaque(false);
            dot.setPreferredSize(new Dimension(28, 28));

            JLabel lbl = new JLabel(names[i], SwingConstants.CENTER);
            lbl.setFont(UIConstants.FONT_SMALL);
            lbl.setForeground(step == currentStep ? UIConstants.PRIMARY : UIConstants.TEXT_MUTED);

            JPanel item = new JPanel();
            item.setOpaque(false);
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
            dot.setAlignmentX(Component.CENTER_ALIGNMENT);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            item.add(dot);
            item.add(Box.createVerticalStrut(2));
            item.add(lbl);
            stepper.add(item);

            if (i < TOTAL_STEPS - 1) {
                JPanel line = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(step < currentStep ? UIConstants.PRIMARY : new Color(0xCBD5E1));
                        g.fillRect(0, getHeight() / 2 - 1, getWidth(), 2);
                    }
                };
                line.setOpaque(false);
                line.setPreferredSize(new Dimension(55, 28));
                stepper.add(line);
            }
        }
        return stepper;
    }

    // =====================================================================
    // STEPS
    // =====================================================================
    private JPanel buildStep1() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JLabel title = new JLabel("Thông tin phòng"); title.setFont(UIConstants.FONT_HEADER);
        JLabel sub   = new JLabel("Xác nhận phòng trước khi tiến hành check-in");
        sub.setFont(UIConstants.FONT_SMALL); sub.setForeground(UIConstants.TEXT_MUTED);

        JPanel phongCard = new JPanel(new BorderLayout());
        phongCard.setBackground(UIConstants.PRIMARY_LIGHT);
        phongCard.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(UIConstants.CARD_RADIUS, UIConstants.PRIMARY),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        // Room selector when no room passed in
        if (selectedPhong == null) {
            java.util.List<Phong> rooms = phongService.getAllPhong();
            // Cho phép chọn phòng chưa đang thuê (có sẵn, đã đặt, bảo trì...) để checkin nhanh.
            rooms.removeIf(r -> "Đang thuê".equals(r.getTrangThai()));
            cboPhong = new JComboBox<>(rooms.toArray(new Phong[0]));
            cboPhong.setFont(UIConstants.FONT_BODY);
            cboPhong.setPreferredSize(new Dimension(0, 34));
            cboPhong.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Phong) {
                        Phong ph = (Phong) value;
                        String view = ph.getView();
                        lbl.setText(String.format("P.%s - %s (%s) - %s", ph.getSoPhong(), ph.getTenLoaiPhong(), view, ph.getTrangThai()));
                    }
                    return lbl;
                }
            });
            if (!rooms.isEmpty()) {
                cboPhong.setSelectedIndex(0);
                selectedPhong = rooms.get(0);
            }
            cboPhong.addActionListener(e -> {
                selectedPhong = (Phong) cboPhong.getSelectedItem();
                refreshHeaderInfo();
            });

            JPanel selectRow = new JPanel(new BorderLayout(6, 0));
            selectRow.setOpaque(false);
            selectRow.add(new JLabel("Chọn phòng:"), BorderLayout.WEST);
            selectRow.add(cboPhong, BorderLayout.CENTER);
            JPanel filling = new JPanel(); filling.setOpaque(false); filling.setLayout(new BoxLayout(filling, BoxLayout.Y_AXIS));
            filling.add(selectRow);
            filling.add(Box.createVerticalStrut(12));
            phongCard.add(filling, BorderLayout.NORTH);
        }

        if (selectedPhong != null) {
            JLabel num   = new JLabel("Phòng " + selectedPhong.getSoPhong()); num.setFont(new Font("Segoe UI",Font.BOLD,20));
            JLabel type  = new JLabel(selectedPhong.getTenLoaiPhong() + " · Tầng " + selectedPhong.getTang() + " · " + selectedPhong.getSucChua() + " người");
            type.setFont(UIConstants.FONT_BODY); type.setForeground(UIConstants.TEXT_SECONDARY);
            JLabel price = new JLabel(String.format("%,.0f đ / đêm", (double) selectedPhong.getGiaTheoNgay()));
            price.setFont(new Font("Segoe UI",Font.BOLD,15)); price.setForeground(UIConstants.PRIMARY);
            JLabel view  = new JLabel("View: " + selectedPhong.getView());
            view.setFont(UIConstants.FONT_SMALL); view.setForeground(UIConstants.TEXT_MUTED);
            JLabel desc  = selectedPhong.getMoTa()!=null ? new JLabel(selectedPhong.getMoTa()) : new JLabel("");
            desc.setFont(UIConstants.FONT_SMALL); desc.setForeground(UIConstants.TEXT_MUTED);

            JPanel info = new JPanel(); info.setOpaque(false); info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
            info.add(num); info.add(Box.createVerticalStrut(4)); info.add(type); info.add(view); info.add(desc); info.add(Box.createVerticalStrut(8)); info.add(price);
            phongCard.add(info, BorderLayout.CENTER);
        }

        // Checklist
        JPanel checklist = new JPanel(); checklist.setOpaque(false); checklist.setLayout(new BoxLayout(checklist,BoxLayout.Y_AXIS));
        JLabel lblChk = new JLabel("KIỂM TRA TRƯỚC KHI NHẬN KHÁCH"); lblChk.setFont(UIConstants.FONT_SMALL_BOLD); lblChk.setForeground(UIConstants.TEXT_SECONDARY);
        checklist.add(Box.createVerticalStrut(12)); checklist.add(lblChk); checklist.add(Box.createVerticalStrut(6));
        for (String c : new String[]{"Phòng sạch sẽ, vệ sinh đầy đủ", "Điều hoà, TV, đèn hoạt động bình thường", "Minibar đầy đủ", "Khăn tắm, ga giường đã thay mới"}) {
            JCheckBox cb = new JCheckBox(c); cb.setFont(UIConstants.FONT_BODY); cb.setOpaque(false);
            checklist.add(cb);
        }

        JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        JPanel hdr = new JPanel(); hdr.setOpaque(false); hdr.setLayout(new BoxLayout(hdr,BoxLayout.Y_AXIS));
        hdr.add(title); hdr.add(Box.createVerticalStrut(2)); hdr.add(sub);
        content.add(hdr); content.add(Box.createVerticalStrut(14)); content.add(phongCard); content.add(checklist);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStep2() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));

        JLabel title = new JLabel("Thông tin khách hàng"); title.setFont(UIConstants.FONT_HEADER);
        JLabel sub   = new JLabel("Nhập CCCD để tìm hoặc điền thông tin khách mới");
        sub.setFont(UIConstants.FONT_SMALL); sub.setForeground(UIConstants.TEXT_MUTED);

        txtCCCD  = styledField("Nhập CCCD / Passport...");
        txtTenKH = styledField("Họ và tên");
        txtSDT   = styledField("Số điện thoại");
        txtEmail = styledField("Email (tùy chọn)");

        if (selectedKhach != null) {
            txtCCCD.setText(selectedKhach.getCccd());
            txtTenKH.setText(selectedKhach.getHoTen());
            txtSDT.setText(selectedKhach.getSoDienThoai());
            txtEmail.setText(selectedKhach.getEmail() != null ? selectedKhach.getEmail() : "");
        }

        JButton btnFind = new JButton("Tìm");
        btnFind.setFont(UIConstants.FONT_SMALL_BOLD);
        btnFind.setBackground(UIConstants.PRIMARY); btnFind.setForeground(Color.WHITE);
        btnFind.setBorderPainted(false); btnFind.setFocusPainted(false);
        btnFind.setPreferredSize(new Dimension(60, 34));
        btnFind.addActionListener(e -> {
            String cccd = txtCCCD.getText().trim();
            if (cccd.isEmpty()) return;
            KhachHang kh = khService.getByCCCD(cccd);
            if (kh != null) {
                selectedKhach = kh;
                txtTenKH.setText(kh.getHoTen());
                txtSDT.setText(kh.getSoDienThoai());
                txtEmail.setText(kh.getEmail() != null ? kh.getEmail() : "");
                JOptionPane.showMessageDialog(this, "Đã tìm thấy: " + kh.getHoTen());
            } else {
                selectedKhach = null;
                JOptionPane.showMessageDialog(this, "Không tìm thấy. Nhập thông tin khách mới bên dưới.");
            }
        });

        JPanel cccdRow = new JPanel(new BorderLayout(6,0)); cccdRow.setOpaque(false);
        cccdRow.add(txtCCCD, BorderLayout.CENTER); cccdRow.add(btnFind, BorderLayout.EAST);

        JPanel fields = new JPanel(new GridLayout(4,1,0,10)); fields.setOpaque(false);
        fields.add(fieldRow("CCCD / Passport", cccdRow));
        fields.add(fieldRow("Họ và tên *",     txtTenKH));
        fields.add(fieldRow("Số điện thoại *", txtSDT));
        fields.add(fieldRow("Email",           txtEmail));

        JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        JPanel hdr = new JPanel(); hdr.setOpaque(false); hdr.setLayout(new BoxLayout(hdr,BoxLayout.Y_AXIS));
        hdr.add(title); hdr.add(Box.createVerticalStrut(2)); hdr.add(sub);
        content.add(hdr); content.add(Box.createVerticalStrut(18)); content.add(fields);
        panel.add(content, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildStep3() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
     
        JLabel title = new JLabel("Ngày & Giờ lưu trú"); title.setFont(UIConstants.FONT_HEADER);
        JLabel sub   = new JLabel("Nhấn vào ô ngày để mở lịch chọn");
        sub.setFont(UIConstants.FONT_SMALL); sub.setForeground(UIConstants.TEXT_MUTED);
     
        // Khởi tạo DateTimePicker
        pickerNgayNhan = new DateTimePicker(selectedDatPhong != null && selectedDatPhong.getNgayNhanDK() != null
            ? selectedDatPhong.getNgayNhanDK() : new Date());
        pickerNgayTra  = new DateTimePicker(selectedDatPhong != null && selectedDatPhong.getNgayTraDK() != null
            ? selectedDatPhong.getNgayTraDK() : new Date(System.currentTimeMillis() + 86_400_000L));
     
        spnSoKhach = new JSpinner(new SpinnerNumberModel(
            selectedDatPhong != null && selectedDatPhong.getSoKhach() > 0 ? selectedDatPhong.getSoKhach() : 1,
            1, 10, 1));
        spnSoKhach.setFont(UIConstants.FONT_BODY);
     
        JPanel fields = new JPanel(new GridLayout(3, 1, 0, 14)); fields.setOpaque(false);
        fields.add(fieldRow("Ngày & giờ nhận phòng *",    pickerNgayNhan));
        fields.add(fieldRow("Ngày & giờ trả (dự kiến) *", pickerNgayTra));
        fields.add(fieldRow("Số khách",                   spnSoKhach));
     
        JPanel content = new JPanel(); content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JPanel hdr = new JPanel(); hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(title); hdr.add(Box.createVerticalStrut(2)); hdr.add(sub);
        content.add(hdr); content.add(Box.createVerticalStrut(18)); content.add(fields);
        panel.add(content, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildStep4() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
        JLabel title = new JLabel("Thêm dịch vụ (nếu có)"); title.setFont(UIConstants.FONT_HEADER);

        allDichVu = dichVuService.getActive();
        String[] names = allDichVu.stream().map(DichVu::getTenDV).toArray(String[]::new);
        cboDV  = new JComboBox<>(names.length > 0 ? names : new String[]{"Không có dịch vụ"}); cboDV.setFont(UIConstants.FONT_BODY);
        spnSLDV = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1)); spnSLDV.setFont(UIConstants.FONT_BODY);
        spnSLDV.setPreferredSize(new Dimension(70, 34));

        JButton btnAddDV = new JButton("+ Thêm"); btnAddDV.setFont(UIConstants.FONT_SMALL_BOLD);
        btnAddDV.setBackground(UIConstants.SUCCESS); btnAddDV.setForeground(Color.WHITE);
        btnAddDV.setBorderPainted(false); btnAddDV.setFocusPainted(false);
        btnAddDV.addActionListener(e -> {
            int idx = cboDV.getSelectedIndex();
            if (idx < 0 || allDichVu.isEmpty()) return;
            DichVu dv = allDichVu.get(idx);
            int sl = (int) spnSLDV.getValue();
            DichVuThue dvt = new DichVuThue("", dv.getMaDV(), sl, dv.getGia());
            dvt.setTenDichVu(dv.getTenDV()); dvt.setDonVi(dv.getDonVi());
            selectedDV.add(dvt);
            refreshDVList();
        });

        JPanel addRow = new JPanel(new BorderLayout(8, 0)); addRow.setOpaque(false);
        addRow.add(cboDV, BorderLayout.CENTER);
        JPanel slBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); slBtn.setOpaque(false);
        JLabel lblSL = new JLabel("SL:"); lblSL.setFont(UIConstants.FONT_BODY);
        slBtn.add(lblSL); slBtn.add(spnSLDV); slBtn.add(btnAddDV);
        addRow.add(slBtn, BorderLayout.EAST);

        dvListPanel = new JPanel(); dvListPanel.setOpaque(false); dvListPanel.setLayout(new BoxLayout(dvListPanel, BoxLayout.Y_AXIS));
        lblTongDV   = new JLabel("Tổng dịch vụ: 0đ"); lblTongDV.setFont(UIConstants.FONT_BODY_BOLD); lblTongDV.setForeground(UIConstants.PRIMARY);

        JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        content.add(title); content.add(Box.createVerticalStrut(16)); content.add(addRow);
        content.add(Box.createVerticalStrut(12)); content.add(dvListPanel);
        content.add(Box.createVerticalStrut(8)); content.add(lblTongDV);
        panel.add(content, BorderLayout.NORTH);
        return panel;
    }

    private void refreshDVList() {
        dvListPanel.removeAll();
        long total = 0;
        for (int i = 0; i < selectedDV.size(); i++) {
            DichVuThue dvt = selectedDV.get(i);
            JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            JLabel info = new JLabel("• " + dvt.getTenDichVu() + "  ×" + dvt.getSoLuong()); info.setFont(UIConstants.FONT_BODY);
            JLabel amt  = new JLabel(String.format("%,.0fđ", (double) dvt.getThanhTien())); amt.setFont(UIConstants.FONT_BODY_BOLD); amt.setForeground(UIConstants.PRIMARY);
            final int idx = i;
            JButton del = new JButton("✕"); del.setFont(UIConstants.FONT_SMALL); del.setForeground(UIConstants.DANGER);
            del.setBorderPainted(false); del.setContentAreaFilled(false); del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            del.addActionListener(e -> { selectedDV.remove(idx); refreshDVList(); });
            JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0)); r.setOpaque(false); r.add(amt); r.add(del);
            row.add(info, BorderLayout.WEST); row.add(r, BorderLayout.EAST);
            dvListPanel.add(row);
            total += dvt.getThanhTien();
        }
        lblTongDV.setText(String.format("Tổng dịch vụ: %,.0fđ", (double) total));
        dvListPanel.revalidate(); dvListPanel.repaint();
    }

    private JPanel buildStep5() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));

        JLabel title = new JLabel("Xác nhận nhận phòng"); title.setFont(UIConstants.FONT_HEADER);
        JLabel sub   = new JLabel("Kiểm tra lại thông tin, sau đó nhấn \"Xác nhận Check-in\"");
        sub.setFont(UIConstants.FONT_SMALL); sub.setForeground(UIConstants.TEXT_MUTED);

        JPanel summary = new JPanel(); summary.setOpaque(false); summary.setLayout(new BoxLayout(summary,BoxLayout.Y_AXIS));
        summary.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(UIConstants.CARD_RADIUS, UIConstants.BORDER),
            BorderFactory.createEmptyBorder(16,16,16,16)));
        summary.setBackground(new Color(0xF8FAFC));

        JLabel lbReady = new JLabel("✓ Sẵn sàng check-in"); lbReady.setFont(new Font("Segoe UI",Font.BOLD,16)); lbReady.setForeground(UIConstants.SUCCESS);
        JLabel hint    = new JLabel("Nhấn \"Xác nhận Check-in\" để hoàn tất"); hint.setFont(UIConstants.FONT_SMALL); hint.setForeground(UIConstants.TEXT_SECONDARY);

        summary.add(lbReady); summary.add(Box.createVerticalStrut(4)); summary.add(hint);

        JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        JPanel hdr = new JPanel(); hdr.setOpaque(false); hdr.setLayout(new BoxLayout(hdr,BoxLayout.Y_AXIS));
        hdr.add(title); hdr.add(Box.createVerticalStrut(2)); hdr.add(sub);
        content.add(hdr); content.add(Box.createVerticalStrut(20)); content.add(summary);
        panel.add(content, BorderLayout.NORTH);
        return panel;
    }

    // =====================================================================
    // BOTTOM BAR & NAVIGATION
    // =====================================================================
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0, UIConstants.BORDER),
            BorderFactory.createEmptyBorder(12, 32, 12, 32)));

        lblStepInfo = new JLabel();
        lblStepInfo.setFont(UIConstants.FONT_SMALL); lblStepInfo.setForeground(UIConstants.TEXT_MUTED);

        btnBack = new RoundedButton("Huỷ", new Color(0xE2E8F0), UIConstants.TEXT_PRIMARY);
        btnNext = new RoundedButton("Tiếp theo →", UIConstants.PRIMARY, Color.WHITE);

        btnBack.addActionListener(e -> { if (currentStep > 1) showStep(currentStep - 1); else dispose(); });
        btnNext.addActionListener(e -> { if (currentStep < TOTAL_STEPS) showStep(currentStep + 1); else doCheckin(); });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btns.setOpaque(false);
        btns.add(btnBack); btns.add(btnNext);

        bar.add(lblStepInfo, BorderLayout.WEST); bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    private void showStep(int step) {
        currentStep = step;
        ((CardLayout) stepContent.getLayout()).show(stepContent, String.valueOf(step));

        // Update header info each step (especially after room selection)
        refreshHeaderInfo();

        // Update button labels
        btnBack.setText(step == 1 ? "Huỷ" : "← Quay lại");
        btnNext.setText(step == TOTAL_STEPS ? "✓ Xác nhận Check-in" : "Tiếp theo →");

        if (step == TOTAL_STEPS) {
            btnNext.setBackground(UIConstants.SUCCESS);
        } else {
            btnNext.setBackground(UIConstants.PRIMARY);
        }
        if (lblStepInfo != null) lblStepInfo.setText("Bước " + step + "/" + TOTAL_STEPS);
        repaint();
    }

    private void refreshHeaderInfo() {
        if (lblInfo == null) return;
        String info = selectedPhong != null
            ? "P." + selectedPhong.getSoPhong() + " · " + selectedPhong.getTenLoaiPhong()
              + " · " + String.format("%,.0fđ/đêm", (double) selectedPhong.getGiaTheoNgay())
            : "Chọn phòng để bắt đầu";
        lblInfo.setText(info);
    }

    // =====================================================================
    // DO CHECK-IN
    // =====================================================================
    private void doCheckin() {
        // Validate step 2
        String tenKH = txtTenKH != null ? txtTenKH.getText().trim() : "";
        String sdt   = txtSDT   != null ? txtSDT.getText().trim()   : "";
        if (tenKH.isEmpty()) { JOptionPane.showMessageDialog(this,"Vui lòng nhập họ tên khách hàng!"); showStep(2); return; }
        if (sdt.isEmpty())   { JOptionPane.showMessageDialog(this,"Vui lòng nhập số điện thoại!"); showStep(2); return; }
        if (selectedPhong == null) { JOptionPane.showMessageDialog(this,"Chưa có phòng được chọn!"); return; }

        Date ngayNhan = pickerNgayNhan.getDate();
        Date ngayTra  = pickerNgayTra.getDate();
        if (!ngayTra.after(ngayNhan)) {
            JOptionPane.showMessageDialog(this, "Ngày trả phải sau ngày nhận!");
            showStep(3);
            return;
        }
        int soKhach = (int) spnSoKhach.getValue();

        // Tạo / tìm khách hàng
        if (selectedKhach == null) {
            KhachHang kh = new KhachHang();
            kh.setHoTen(tenKH); kh.setSoDienThoai(sdt);
            kh.setCccd(txtCCCD != null ? txtCCCD.getText().trim() : "");
            kh.setEmail(txtEmail != null ? txtEmail.getText().trim() : "");
            kh.setHang("Thường"); kh.setTrangThai("Đang ở");
            String err = khService.them(kh);
            if (err != null) { JOptionPane.showMessageDialog(this, "Lỗi tạo khách: " + err); return; }
            selectedKhach = khService.getByCCCD(kh.getCccd());
            if (selectedKhach == null) { selectedKhach = kh; }
        } else {
            selectedKhach.setTrangThai("Đang ở");
            khService.sua(selectedKhach);
        }

        // Tạo ThuePhong
        ThuePhong tp = new ThuePhong();
        tp.setMaKH(selectedKhach.getMaKH());
        tp.setSoPhong(selectedPhong.getSoPhong());
        tp.setNgayNhan(ngayNhan); tp.setNgayTraDK(ngayTra);
        tp.setSoKhach(soKhach);
        tp.setMaNV(AuthService.getInstance().getCurrentMaNV());

        String err = thuePhongService.checkIn(tp);
        if (err != null) { JOptionPane.showMessageDialog(this, "Lỗi check-in: " + err); return; }

        // Lưu các dịch vụ đã chọn vào DB
        for (DichVuThue dvt : selectedDV) {
            thuePhongService.themDichVu(
                tp.getMaThue(),
                dvt.getMaDV(),
                dvt.getSoLuong(),
                dvt.getDonGia()
            );
        }

        confirmed = true;

        // Cập nhật trạng thái đặt phòng nếu có
        if (selectedDatPhong != null) {
            datPhongService.updateTrangThai(selectedDatPhong.getMaDatPhong(), "Đã checkin");
        }

        JOptionPane.showMessageDialog(this,
            "✓ Check-in thành công!\nMã phiếu: " + tp.getMaThue(),
            "Thành công", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }

    // ---- Helpers ----
    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(UIConstants.FONT_BODY);
        f.setToolTipText(placeholder);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(UIConstants.BTN_RADIUS, UIConstants.BORDER),
            BorderFactory.createEmptyBorder(5, 9, 5, 9)));
        f.setPreferredSize(new Dimension(0, 34));
        return f;
    }

    private JPanel fieldRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 3)); p.setOpaque(false);
        JLabel lbl = new JLabel(label); lbl.setFont(UIConstants.FONT_SMALL_BOLD);
        lbl.setPreferredSize(new Dimension(160, 16));
        p.add(lbl, BorderLayout.NORTH); p.add(comp, BorderLayout.CENTER);
        return p;
    }
}
