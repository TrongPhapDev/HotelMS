package UI.dialogs;

import service.*;
import entity.*;
import UI.components.UIConstants;
import UI.components.RoundedComponents.*;

import javax.swing.*;
import java.awt.*;

// ====================================================================
// LoaiPhongDialog
// ====================================================================
public class LoaiPhongDialog extends JDialog {
    private final PhongService service = new PhongService();
    private final LoaiPhong    entity;
    private boolean confirmed = false;

    private JTextField    txtTen, txtSucChua, txtGiaThap, txtGiaCao, txtTiNghi, txtMoTa;
    private JComboBox<String> cboDanhMuc, cboTrangThai;

    public LoaiPhongDialog(Frame parent, LoaiPhong lp) {
        super(parent, lp == null ? "Thêm loại phòng mới" : "Chỉnh sửa – " + lp.getTenLoai(), true);
        this.entity = lp;
        setSize(560, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        if (lp != null) fillData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(16,24,16,24));
        JLabel title = new JLabel(entity==null?"Thêm loại phòng mới":"Chỉnh sửa loại phòng");
        title.setFont(new Font("Segoe UI",Font.BOLD,17)); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel(entity!=null?"Mã: "+entity.getMaLoai():"Mã sẽ được tự động tạo");
        sub.setFont(UIConstants.FONT_SMALL); sub.setForeground(new Color(255,255,255,180));
        JPanel hl = new JPanel(); hl.setOpaque(false); hl.setLayout(new BoxLayout(hl,BoxLayout.Y_AXIS));
        hl.add(title); hl.add(sub);
        header.add(hl, BorderLayout.WEST);

        // Form
        JPanel body = new JPanel(new GridLayout(0,2,14,12));
        body.setBackground(Color.WHITE); body.setBorder(BorderFactory.createEmptyBorder(20,24,8,24));

        txtTen      = f(); txtSucChua = f(); txtSucChua.setText("2");
        txtGiaThap  = f(); txtGiaCao  = f();
        txtTiNghi   = f(); txtMoTa    = f();
        cboDanhMuc  = cb("ECONOMY","STANDARD","SUPERIOR","DELUXE","FAMILY","SUITE","VIP");
        cboTrangThai= cb("Hoạt động","Ngừng");

        body.add(lf("Tên loại phòng *", txtTen));  body.add(lf("Danh mục", cboDanhMuc));
        body.add(lf("Sức chứa (người)", txtSucChua)); body.add(lf("Trạng thái", cboTrangThai));
        body.add(lf("Giá thấp nhất (đ)",txtGiaThap)); body.add(lf("Giá cao nhất (đ)", txtGiaCao));
        body.add(lf("Tiện nghi",        txtTiNghi)); body.add(lf("Mô tả", txtMoTa));

        // Footer
        JPanel footer = footer();
        RoundedButton btnCancel = new RoundedButton("Huỷ", new Color(0xE2E8F0), UIConstants.TEXT_PRIMARY);
        RoundedButton btnSave   = new RoundedButton(entity==null?"Thêm loại phòng":"Lưu thay đổi", UIConstants.PRIMARY, Color.WHITE);
        btnCancel.addActionListener(e -> dispose()); btnSave.addActionListener(e -> doSave());
        getRootPane().setDefaultButton(btnSave);
        footer.add(btnCancel); footer.add(btnSave);

        root.add(header,BorderLayout.NORTH); root.add(body,BorderLayout.CENTER); root.add(footer,BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void fillData() {
        txtTen.setText(entity.getTenLoai()); txtSucChua.setText(String.valueOf(entity.getSucChua()));
        txtGiaThap.setText(String.valueOf(entity.getGiaThapNhat())); txtGiaCao.setText(String.valueOf(entity.getGiaCaoNhat()));
        txtTiNghi.setText(entity.getTiNghi()!=null?entity.getTiNghi():"");
        txtMoTa.setText(entity.getMoTa()!=null?entity.getMoTa():"");
        sc(cboDanhMuc, entity.getDanhMuc()); sc(cboTrangThai, entity.getTrangThai());
    }

    private void doSave() {
        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) { err("Tên loại phòng không được để trống!"); txtTen.requestFocus(); return; }
        int sucChua = 2;
        try { sucChua = Integer.parseInt(txtSucChua.getText().trim()); if(sucChua<1) throw new Exception(); }
        catch(Exception e) { err("Sức chứa phải là số nguyên dương!"); txtSucChua.requestFocus(); return; }
        long giaThap=0, giaCao=0;
        try { giaThap = Long.parseLong(txtGiaThap.getText().trim().replace(",","")); } catch(Exception e){}
        try { giaCao  = Long.parseLong(txtGiaCao.getText().trim().replace(",",""));  } catch(Exception e){}
        if (giaCao < giaThap) { err("Giá cao nhất phải ≥ giá thấp nhất!"); return; }

        LoaiPhong lp = entity!=null?entity:new LoaiPhong();
        lp.setTenLoai(ten); lp.setSucChua(sucChua); lp.setGiaThapNhat(giaThap); lp.setGiaCaoNhat(giaCao);
        lp.setTiNghi(txtTiNghi.getText().trim()); lp.setMoTa(txtMoTa.getText().trim());
        lp.setDanhMuc((String)cboDanhMuc.getSelectedItem()); lp.setTrangThai((String)cboTrangThai.getSelectedItem());

        String error = entity==null?service.themLoaiPhong(lp):service.suaLoaiPhong(lp);
        if (error==null) { confirmed=true; dispose(); } else err(error);
    }

    public boolean isConfirmed() { return confirmed; }
    private JTextField f() { JTextField t=new JTextField(); t.setFont(UIConstants.FONT_BODY); t.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(UIConstants.BTN_RADIUS,UIConstants.BORDER),BorderFactory.createEmptyBorder(5,9,5,9))); t.setPreferredSize(new Dimension(0,34)); return t; }
    private JComboBox<String> cb(String... i) { JComboBox<String> c=new JComboBox<>(i); c.setFont(UIConstants.FONT_BODY); return c; }
    private JPanel lf(String l, JComponent c) { JPanel p=new JPanel(new BorderLayout(0,3)); p.setOpaque(false); JLabel lb=new JLabel(l); lb.setFont(UIConstants.FONT_SMALL_BOLD); p.add(lb,BorderLayout.NORTH); p.add(c,BorderLayout.CENTER); return p; }
    private JPanel footer() { JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10)); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UIConstants.BORDER)); return p; }
    private void sc(JComboBox<String> c, String v) { if(v==null)return; for(int i=0;i<c.getItemCount();i++) if(v.equals(c.getItemAt(i))){c.setSelectedIndex(i);return;} }
    private void err(String m) { JOptionPane.showMessageDialog(this,m,"Lỗi dữ liệu",JOptionPane.WARNING_MESSAGE); }
}
