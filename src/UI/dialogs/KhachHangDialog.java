package UI.dialogs;

import UI.components.RoundedComponents.*;
import UI.components.RoundedComponents.RoundedBorder;
import UI.components.RoundedComponents.RoundedButton;
import UI.components.UIConstants;
import entity.KhachHang;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import service.KhachHangService;

public class KhachHangDialog extends JDialog {

    private final KhachHangService service = new KhachHangService();
    private final KhachHang        entity;
    private boolean confirmed = false;

    private JTextField    txtTen, txtSDT, txtCCCD, txtEmail, txtDiaChi, txtQuocTich;
    private JComboBox<String> cboGioiTinh, cboHang, cboTrangThai;
    private JLabel        lblMa;
    private RoundedButton btnSave;
    private boolean       hasChanges = false;

    public KhachHangDialog(Frame parent, KhachHang kh) {
        super(parent, kh == null ? "Thêm khách hàng mới" : "Chỉnh sửa – " + kh.getHoTen(), true);
        this.entity = kh;
        setSize(560, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        if (kh != null) fillData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel(entity == null ? "Thêm khách hàng mới" : "Chỉnh sửa khách hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        lblMa = new JLabel(entity != null ? "Mã: " + entity.getMaKH() : "");
        lblMa.setFont(UIConstants.FONT_SMALL); lblMa.setForeground(new Color(255,255,255,180));
        JPanel hLeft = new JPanel(); hLeft.setOpaque(false);
        hLeft.setLayout(new BoxLayout(hLeft, BoxLayout.Y_AXIS));
        hLeft.add(title); hLeft.add(lblMa);
        header.add(hLeft, BorderLayout.WEST);

        // Form body
        JPanel body = new JPanel(new GridLayout(0, 2, 14, 12));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));

        txtTen      = field(); txtSDT    = field(); txtCCCD  = field();
        txtEmail    = field(); txtDiaChi = field(); txtQuocTich = field();
        txtQuocTich.setText("Việt Nam");
        cboGioiTinh  = combo("Nam", "Nữ", "Khác");
        cboHang      = combo("Thường", "Silver", "Gold", "VIP");
        cboTrangThai = combo("Đã trả", "Đang ở");

        body.add(labelField("Họ và tên *",    txtTen));
        body.add(labelField("Giới tính",       cboGioiTinh));
        body.add(labelField("Số điện thoại *", txtSDT));
        body.add(labelField("CCCD / Passport", txtCCCD));
        body.add(labelField("Email",           txtEmail));
        body.add(labelField("Quốc tịch",       txtQuocTich));
        body.add(labelField("Địa chỉ",         txtDiaChi));
        body.add(labelField("Hạng khách",      cboHang));
        body.add(new JLabel());
        body.add(labelField("Trạng thái",      cboTrangThai));

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER));

        RoundedButton btnCancel = new RoundedButton("Huỷ",  new Color(0xE2E8F0), UIConstants.TEXT_PRIMARY);
        btnSave = new RoundedButton(entity == null ? "Thêm khách hàng mới" : "Lưu thay đổi",
                                                    UIConstants.PRIMARY, Color.WHITE);
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e   -> doSave());

        setupChangeTracking();

        // Enter key = save
        getRootPane().setDefaultButton(btnSave);

        footer.add(btnCancel);
        footer.add(btnSave);

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void fillData() {
        txtTen.setText(entity.getHoTen());
        txtSDT.setText(entity.getSoDienThoai());
        txtCCCD.setText(entity.getCccd()     != null ? entity.getCccd()     : "");
        txtEmail.setText(entity.getEmail()   != null ? entity.getEmail()    : "");
        txtDiaChi.setText(entity.getDiaChi() != null ? entity.getDiaChi()   : "");
        txtQuocTich.setText(entity.getQuocTich() != null ? entity.getQuocTich() : "Việt Nam");
        setCombo(cboGioiTinh,  entity.getGioiTinh());
        setCombo(cboHang,      entity.getHang());
        setCombo(cboTrangThai, entity.getTrangThai());
    }

    private void doSave() {
        // Validation
        String ten = txtTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        if (ten.isEmpty()) { showError("Họ và tên không được để trống!"); txtTen.requestFocus(); return; }
        if (sdt.isEmpty()) { showError("Số điện thoại không được để trống!"); txtSDT.requestFocus(); return; }
        if (!sdt.matches("\\d{9,11}")) { showError("Số điện thoại phải có 9–11 chữ số!"); txtSDT.requestFocus(); return; }

        KhachHang kh = entity != null ? entity : new KhachHang();
        kh.setHoTen(ten);
        kh.setSoDienThoai(sdt);
        kh.setCccd(txtCCCD.getText().trim());
        kh.setEmail(txtEmail.getText().trim());
        kh.setDiaChi(txtDiaChi.getText().trim());
        kh.setQuocTich(txtQuocTich.getText().trim().isEmpty() ? "Việt Nam" : txtQuocTich.getText().trim());
        kh.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        kh.setHang((String) cboHang.getSelectedItem());
        kh.setTrangThai((String) cboTrangThai.getSelectedItem());
        if (entity == null) { kh.setSoLanLuuTru(0); kh.setTongChiTieu(0); kh.setNgayTao(new Date()); }

        String err = entity == null ? service.them(kh) : service.sua(kh);
        if (err == null) {
            confirmed = true;
            dispose();
        } else {
            showError(err);
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }

    // ---- Helpers ----
    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(UIConstants.BTN_RADIUS, UIConstants.BORDER),
            BorderFactory.createEmptyBorder(5, 9, 5, 9)));
        f.setPreferredSize(new Dimension(0, 34));
        return f;
    }

    private JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(UIConstants.FONT_BODY);
        c.setPreferredSize(new Dimension(0, 34));
        return c;
    }

    private JPanel labelField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL_BOLD);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void setCombo(JComboBox<String> c, String val) {
        if (val == null) return;
        for (int i = 0; i < c.getItemCount(); i++)
            if (val.equals(c.getItemAt(i))) { c.setSelectedIndex(i); return; }
    }

    private void setupChangeTracking() {
        if (entity == null) {
            hasChanges = true;
            btnSave.setEnabled(true);
            return;
        }

        btnSave.setEnabled(false);

        DocumentListener doc = new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { markChanged(); }
            public void removeUpdate(DocumentEvent e)  { markChanged(); }
            public void changedUpdate(DocumentEvent e) { markChanged(); }
        };

        txtTen.getDocument().addDocumentListener(doc);
        txtSDT.getDocument().addDocumentListener(doc);
        txtCCCD.getDocument().addDocumentListener(doc);
        txtEmail.getDocument().addDocumentListener(doc);
        txtDiaChi.getDocument().addDocumentListener(doc);
        txtQuocTich.getDocument().addDocumentListener(doc);

        ActionListener act = e -> markChanged();
        cboGioiTinh.addActionListener(act);
        cboHang.addActionListener(act);
        cboTrangThai.addActionListener(act);
    }

    private void markChanged() {
        hasChanges = true;
        btnSave.setEnabled(true);
    }
}

