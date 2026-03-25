package UI.dialogs;

import service.NhanVienService;
import entity.NhanVien;
import UI.components.UIConstants;
import UI.components.RoundedComponents.*;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class NhanVienDialog extends JDialog {

    private final NhanVienService service = new NhanVienService();
    private final NhanVien        entity;
    private boolean confirmed = false;

    private JTextField    txtTen, txtSDT, txtEmail, txtDiaChi, txtHeSo, txtMatKhau;
    private JComboBox<String> cboGioiTinh, cboChucVu, cboTrangThai, cboVaiTro;

    public NhanVienDialog(Frame parent, NhanVien nv) {
        super(parent, nv == null ? "Thêm nhân viên mới" : "Chỉnh sửa – " + nv.getHoTen(), true);
        this.entity = nv;
        setSize(560, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        if (nv != null) fillData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel(entity == null ? "Thêm nhân viên mới" : "Chỉnh sửa nhân viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17)); title.setForeground(Color.WHITE);
        JLabel lblMa = new JLabel(entity != null ? "Mã: " + entity.getMaNV() : "Mã sẽ được tự động tạo");
        lblMa.setFont(UIConstants.FONT_SMALL); lblMa.setForeground(new Color(255,255,255,180));
        JPanel hLeft = new JPanel(); hLeft.setOpaque(false); hLeft.setLayout(new BoxLayout(hLeft,BoxLayout.Y_AXIS));
        hLeft.add(title); hLeft.add(lblMa);
        header.add(hLeft, BorderLayout.WEST);

        // Form
        JPanel body = new JPanel(new GridLayout(0, 2, 14, 12));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));

        txtTen     = field(); txtSDT   = field(); txtEmail  = field();
        txtDiaChi  = field(); txtHeSo  = field(); txtHeSo.setText("1.0");
        txtMatKhau = new JPasswordField(); styleField(txtMatKhau);
        if (entity == null) txtMatKhau.setText("123456");

        cboGioiTinh  = combo("Nam","Nữ","Khác");
        cboChucVu    = combo("Quản lý","Nhân viên","Lễ tân","Bảo vệ","Kỹ thuật","Bếp");
        cboTrangThai = combo("Hoạt động","Nghỉ phép","Đã nghỉ việc");
        cboVaiTro    = combo("STAFF","MANAGER","ADMIN");

        body.add(labelField("Họ và tên *",    txtTen));
        body.add(labelField("Giới tính",       cboGioiTinh));
        body.add(labelField("Số điện thoại *", txtSDT));
        body.add(labelField("Email",           txtEmail));
        body.add(labelField("Địa chỉ",         txtDiaChi));
        body.add(labelField("Chức vụ",         cboChucVu));
        body.add(labelField("Hệ số lương",     txtHeSo));
        body.add(labelField("Vai trò hệ thống",cboVaiTro));
        body.add(labelField("Trạng thái",      cboTrangThai));
        body.add(labelField("Mật khẩu" + (entity == null ? " *" : " (để trống = giữ nguyên)"), txtMatKhau));

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UIConstants.BORDER));
        RoundedButton btnCancel = new RoundedButton("Huỷ",  new Color(0xE2E8F0), UIConstants.TEXT_PRIMARY);
        RoundedButton btnSave   = new RoundedButton(entity == null ? "Thêm nhân viên" : "Lưu thay đổi",
                                                    UIConstants.PRIMARY, Color.WHITE);
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e   -> doSave());
        getRootPane().setDefaultButton(btnSave);
        footer.add(btnCancel); footer.add(btnSave);

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void fillData() {
        txtTen.setText(entity.getHoTen());
        txtSDT.setText(entity.getSoDienThoai());
        txtEmail.setText(entity.getEmail()   != null ? entity.getEmail()   : "");
        txtDiaChi.setText(entity.getDiaChi() != null ? entity.getDiaChi()  : "");
        txtHeSo.setText(String.format("%.1f", entity.getHeSoLuong()));
        if (entity.getMatKhau() != null) ((JPasswordField) txtMatKhau).setText(entity.getMatKhau());
        setCombo(cboGioiTinh,  entity.getGioiTinh());
        setCombo(cboChucVu,    entity.getChucVu());
        setCombo(cboTrangThai, entity.getTrangThai());
        setCombo(cboVaiTro,    entity.getVaiTro());
    }

    private void doSave() {
        String ten = txtTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        if (ten.isEmpty()) { showErr("Họ và tên không được để trống!"); txtTen.requestFocus(); return; }
        if (sdt.isEmpty()) { showErr("Số điện thoại không được để trống!"); txtSDT.requestFocus(); return; }
        if (!sdt.matches("\\d{9,11}")) { showErr("Số điện thoại phải có 9–11 chữ số!"); txtSDT.requestFocus(); return; }

        double heso = 1.0;
        try { heso = Double.parseDouble(txtHeSo.getText().trim()); if (heso <= 0) throw new Exception(); }
        catch (Exception e) { showErr("Hệ số lương phải là số dương!"); txtHeSo.requestFocus(); return; }

        NhanVien nv = entity != null ? entity : new NhanVien();
        nv.setHoTen(ten); nv.setSoDienThoai(sdt);
        nv.setEmail(txtEmail.getText().trim()); nv.setDiaChi(txtDiaChi.getText().trim());
        nv.setHeSoLuong(heso);
        nv.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        nv.setChucVu((String) cboChucVu.getSelectedItem());
        nv.setTrangThai((String) cboTrangThai.getSelectedItem());
        nv.setVaiTro((String) cboVaiTro.getSelectedItem());

        String pw = new String(((JPasswordField) txtMatKhau).getPassword()).trim();
        if (pw.isEmpty()) {
            if (entity == null) pw = "123456";  // Default cho add mới
            else pw = entity.getMatKhau();      // Giữ nguyên khi edit
        }
        nv.setMatKhau(pw);
        System.out.println("💾 Save " + (entity == null ? "ADD" : "EDIT") + " nhân viên: maNV=" + (entity == null ? "auto" : entity.getMaNV()) + ", password=" + pw);

        if (entity == null) nv.setNgayVaoLam(new Date());

        String err = entity == null ? service.them(nv) : service.sua(nv);
        if (err == null) { confirmed = true; dispose(); }
        else showErr(err);
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }

    private JTextField field() { JTextField f = new JTextField(); styleField(f); return f; }
    private void styleField(JTextField f) {
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(UIConstants.BTN_RADIUS, UIConstants.BORDER),
            BorderFactory.createEmptyBorder(5,9,5,9)));
        f.setPreferredSize(new Dimension(0,34));
    }
    private JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items); c.setFont(UIConstants.FONT_BODY); c.setPreferredSize(new Dimension(0,34)); return c;
    }
    private JPanel labelField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0,3)); p.setOpaque(false);
        JLabel lbl = new JLabel(label); lbl.setFont(UIConstants.FONT_SMALL_BOLD); lbl.setForeground(UIConstants.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.NORTH); p.add(comp, BorderLayout.CENTER); return p;
    }
    private void setCombo(JComboBox<String> c, String val) {
        if (val==null) return;
        for (int i=0;i<c.getItemCount();i++) if (val.equals(c.getItemAt(i))) { c.setSelectedIndex(i); return; }
    }
}
