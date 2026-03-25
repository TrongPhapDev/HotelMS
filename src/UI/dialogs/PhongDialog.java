package UI.dialogs;

import service.PhongService;
import entity.*;
import UI.components.UIConstants;
import UI.components.RoundedComponents.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PhongDialog extends JDialog {

    private final PhongService service = new PhongService();
    private final Phong        entity;
    private boolean confirmed = false;

    private JTextField    txtSoPhong, txtTang, txtSucChua, txtGia, txtMoTa;
    private JComboBox<String> cboLoai, cboView, cboTrangThai;
    private List<LoaiPhong> loaiList;

    public PhongDialog(Frame parent, Phong p) {
        super(parent, p == null ? "Thêm phòng mới" : "Chỉnh sửa – Phòng " + p.getSoPhong(), true);
        this.entity = p;
        setSize(540, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        if (p != null) fillData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(16,24,16,24));
        JLabel title = new JLabel(entity==null?"Thêm phòng mới":"Chỉnh sửa phòng "+entity.getSoPhong());
        title.setFont(new Font("Segoe UI",Font.BOLD,17)); title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Load loại phòng
        loaiList = service.getActiveLoaiPhong();
        String[] loaiNames = loaiList.stream()
            .map(lp -> lp.getMaLoai() + " – " + lp.getTenLoai()).toArray(String[]::new);
        cboLoai = new JComboBox<>(loaiNames); cboLoai.setFont(UIConstants.FONT_BODY);
        String[] views = new String[]{"View thường", "View biển", "View núi"};
        cboView = new JComboBox<>(views); cboView.setFont(UIConstants.FONT_BODY);
        cboTrangThai = new JComboBox<>(new String[]{"Có sẵn","Đang thuê","Đã đặt","Vệ sinh","Bảo trì"});
        cboTrangThai.setFont(UIConstants.FONT_BODY);

        txtSoPhong = f(); txtTang    = f(); txtTang.setText("1");
        txtSucChua = f(); txtSucChua.setText("2");
        txtGia     = f(); txtGia.setText("500000");
        txtMoTa    = f();

        if (entity != null) { txtSoPhong.setEditable(false); txtSoPhong.setBackground(new Color(0xF1F5F9)); }

        JPanel body = new JPanel(new GridLayout(0,2,14,12));
        body.setBackground(Color.WHITE); body.setBorder(BorderFactory.createEmptyBorder(20,24,8,24));
        body.add(lf("Số phòng *",    txtSoPhong));   body.add(lf("Tầng",       txtTang));
        body.add(lf("Loại phòng *",  cboLoai));       body.add(lf("View",        cboView));
        body.add(lf("Sức chứa",      txtSucChua));   body.add(lf("Trạng thái", cboTrangThai));
        body.add(lf("Giá/đêm (đ) *", txtGia));        body.add(lf("Mô tả",      txtMoTa));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UIConstants.BORDER));
        RoundedButton btnCancel = new RoundedButton("Huỷ", new Color(0xE2E8F0), UIConstants.TEXT_PRIMARY);
        RoundedButton btnSave   = new RoundedButton(entity==null?"Thêm phòng":"Lưu thay đổi", UIConstants.PRIMARY, Color.WHITE);
        btnCancel.addActionListener(e -> dispose()); btnSave.addActionListener(e -> doSave());
        getRootPane().setDefaultButton(btnSave);
        footer.add(btnCancel); footer.add(btnSave);

        root.add(header,BorderLayout.NORTH); root.add(body,BorderLayout.CENTER); root.add(footer,BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void fillData() {
        txtSoPhong.setText(entity.getSoPhong());
        txtTang.setText(String.valueOf(entity.getTang()));
        txtSucChua.setText(String.valueOf(entity.getSucChua()));
        txtGia.setText(String.valueOf(entity.getGiaTheoNgay()));
        txtMoTa.setText(entity.getMoTa()!=null?entity.getMoTa():"");
        // Select loại phòng
        for (int i=0;i<loaiList.size();i++)
            if (loaiList.get(i).getMaLoai().equals(entity.getMaLoai())) { cboLoai.setSelectedIndex(i); break; }

        // Chuyển giá trị view nếu chưa có tiền tố (vì constraint ở DB có thể yêu cầu "View ...")
        String currentView = entity.getLoaiView();
        if (currentView == null || currentView.isBlank()) currentView = entity.getView();
        if (currentView != null && !currentView.isBlank()) {
            if (!currentView.toLowerCase().startsWith("view")) {
                currentView = "View " + currentView;
            }
        }
        sc(cboView, currentView);

        sc(cboTrangThai, entity.getTrangThai());
    }

    private void doSave() {
        String soPhong = txtSoPhong.getText().trim();
        if (soPhong.isEmpty()) { err("Số phòng không được để trống!"); txtSoPhong.requestFocus(); return; }
        if (loaiList.isEmpty()) { err("Chưa có loại phòng nào trong hệ thống!"); return; }

        int tang; 
        try { tang     = Integer.parseInt(txtTang.getText().trim());     if(tang<1)     throw new Exception(); } catch(Exception e){ err("Tầng phải là số nguyên dương!"); return; }
        int sucChua;
        try { sucChua  = Integer.parseInt(txtSucChua.getText().trim());  if(sucChua<1)  throw new Exception(); } catch(Exception e){ err("Sức chứa phải là số nguyên dương!"); return; }
        long gia;
        try { gia      = Long.parseLong(txtGia.getText().trim().replace(",",""));        if(gia<=0) throw new Exception(); } catch(Exception e){ err("Giá/đêm phải là số dương!"); return; }

        Phong p = entity!=null?entity:new Phong();
        p.setSoPhong(soPhong);
        int idx = cboLoai.getSelectedIndex();
        if (idx>=0 && idx<loaiList.size()) p.setMaLoai(loaiList.get(idx).getMaLoai());
        p.setTang(tang); p.setSucChua(sucChua); p.setGiaTheoNgay(gia);
        p.setMoTa(txtMoTa.getText().trim());
        p.setView((String)cboView.getSelectedItem());
        p.setTrangThai((String)cboTrangThai.getSelectedItem());

        String error = entity==null?service.themPhong(p):service.suaPhong(p);
        if (error==null) { confirmed=true; dispose(); } else err(error);
    }

    public boolean isConfirmed() { return confirmed; }
    private JTextField f() { JTextField t=new JTextField(); t.setFont(UIConstants.FONT_BODY); t.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(UIConstants.BTN_RADIUS,UIConstants.BORDER),BorderFactory.createEmptyBorder(5,9,5,9))); t.setPreferredSize(new Dimension(0,34)); return t; }
    private JPanel lf(String l, JComponent c) { JPanel p=new JPanel(new BorderLayout(0,3)); p.setOpaque(false); JLabel lb=new JLabel(l); lb.setFont(UIConstants.FONT_SMALL_BOLD); p.add(lb,BorderLayout.NORTH); p.add(c,BorderLayout.CENTER); return p; }
    private void sc(JComboBox<String> c, String v) { if(v==null)return; for(int i=0;i<c.getItemCount();i++) if(v.equals(c.getItemAt(i))){c.setSelectedIndex(i);return;} }
    private void err(String m) { JOptionPane.showMessageDialog(this,m,"Lỗi dữ liệu",JOptionPane.WARNING_MESSAGE); }
}
