package UI.panels;

import UI.MainFrame;
import UI.components.ExcelExporter;
import UI.components.RoundedComponents.ModernTextField;
import UI.components.RoundedComponents.RoundedBorder;
import UI.components.RoundedComponents.RoundedButton;
import UI.components.RoundedComponents.RoundedPanel;
import UI.components.UIConstants;
import UI.dialogs.KhachHangDialog;
import entity.KhachHang;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import service.KhachHangService;

public class KhachHangPanel extends JPanel {

    @SuppressWarnings("unused")
    private final MainFrame        mainFrame;
    private final KhachHangService service = new KhachHangService();

    private ModernTextField   txtSearch;
    private JTable            table;
    private DefaultTableModel tableModel;
    private String            currentHang = "Tất cả";
    private JLabel            lblFooter;
    private RoundedButton     btnSua, btnXoa;

    public KhachHangPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        main.add(buildHeader(),  BorderLayout.NORTH);
        main.add(buildStats(),   BorderLayout.CENTER);

        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.add(Box.createVerticalStrut(14), BorderLayout.NORTH);
        bot.add(buildFilterBar(), BorderLayout.CENTER);
        bot.add(buildTableArea(), BorderLayout.SOUTH);
        main.add(bot, BorderLayout.SOUTH);
        add(main, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Khách hàng"); t.setFont(UIConstants.FONT_TITLE);
        JLabel s = new JLabel("Quản lý hồ sơ và lịch sử lưu trú");
        s.setFont(UIConstants.FONT_BODY); s.setForeground(UIConstants.TEXT_SECONDARY);
        left.add(t); left.add(Box.createVerticalStrut(2)); left.add(s);

        btnSua = new RoundedButton("Sửa", new Color(0xE2E8F0), UIConstants.TEXT_PRIMARY);
        btnXoa = new RoundedButton("Xóa", UIConstants.DANGER, Color.WHITE);
        RoundedButton btnThem  = new RoundedButton("+ Thêm khách hàng", UIConstants.PRIMARY, Color.WHITE);
        RoundedButton btnExcel = new RoundedButton("↓ Xuất Excel", new Color(0x16A34A), Color.WHITE);
        btnSua.setEnabled(false); btnXoa.setEnabled(false);
        btnSua.addActionListener(e   -> editSelected());
        btnXoa.addActionListener(e   -> deleteSelected());
        btnThem.addActionListener(e  -> showDialog(null));
        btnExcel.addActionListener(e -> {
            String kw   = txtSearch!=null ? txtSearch.getText().trim() : "";
            String hang = "Tất cả".equals(currentHang) ? null : currentHang;
            ExcelExporter.exportKhachHang(KhachHangPanel.this,
                service.search(kw.isEmpty()?null:kw, hang));
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(btnExcel); btns.add(btnSua); btns.add(btnXoa); btns.add(btnThem);
        p.add(left, BorderLayout.WEST); p.add(btns, BorderLayout.EAST);
        return p;
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        int tong = service.getAll().size(), vipGold = service.countVipGold(), dangO = service.countDangO();
        long dt = service.getTongDoanhThu();
        row.add(statCard("Tổng khách hàng", tong    + " KH", UIConstants.PRIMARY));
        row.add(statCard("VIP + Gold",       vipGold + " KH", UIConstants.INFO));
        row.add(statCard("Đang lưu trú",     dangO   + " KH", UIConstants.SUCCESS));
        row.add(statCard("Tổng doanh thu",
            String.format("%,.1f tr đ", dt / 1_000_000.0), UIConstants.WARNING));
        return row;
    }

    private RoundedPanel statCard(String label, String value, Color c) {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(Color.WHITE); card.setShadow(true);
        card.setLayout(new BorderLayout()); card.setBorder(BorderFactory.createEmptyBorder(14,16,8,16));
        JLabel l1 = new JLabel(label); l1.setFont(UIConstants.FONT_SMALL); l1.setForeground(UIConstants.TEXT_SECONDARY);
        JLabel l2 = new JLabel(value); l2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JPanel bar = new JPanel() { @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create(); g2.setColor(c); g2.fillRoundRect(0,3,getWidth(),3,3,3); g2.dispose(); } };
        bar.setOpaque(false); bar.setPreferredSize(new Dimension(0,8));
        JPanel ct = new JPanel(); ct.setOpaque(false); ct.setLayout(new BoxLayout(ct,BoxLayout.Y_AXIS));
        ct.add(l1); ct.add(Box.createVerticalStrut(4)); ct.add(l2);
        card.add(ct, BorderLayout.CENTER); card.add(bar, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        txtSearch = new ModernTextField("Tìm tên, SĐT, CCCD, email...");
        txtSearch.setPreferredSize(new Dimension(260, 36));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e)  { loadTable(); }
            @Override
            public void removeUpdate(DocumentEvent e)  { loadTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { loadTable(); }
        });
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tabs.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        for (String h : new String[]{"Tất cả","VIP","Gold","Silver","Thường"}) {
            JToggleButton btn = new JToggleButton(h);
            btn.setFont(UIConstants.FONT_SMALL_BOLD); btn.setFocusPainted(false);
            btn.setSelected(h.equals(currentHang));
            styleTab(btn, h.equals(currentHang));
            btn.addActionListener(e -> {
                currentHang = h;
                for (Component c : tabs.getComponents())
                    if (c instanceof JToggleButton) { JToggleButton tb = (JToggleButton) c; styleTab(tb, tb.getText().equals(h)); }
                loadTable();
            });
            bg.add(btn); tabs.add(btn);
        }
        p.add(txtSearch, BorderLayout.WEST); p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    private void styleTab(JToggleButton btn, boolean on) {
        if (on) {
            btn.setBackground(UIConstants.PRIMARY); btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(16, UIConstants.PRIMARY), BorderFactory.createEmptyBorder(5,12,5,12)));
        } else {
            btn.setBackground(Color.WHITE); btn.setForeground(UIConstants.TEXT_SECONDARY);
            btn.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(16, UIConstants.BORDER), BorderFactory.createEmptyBorder(5,12,5,12)));
        }
    }

    private JPanel buildTableArea() {
        String[] cols = {"_ma","Khách hàng","Liên hệ","CCCD","Quốc tịch","Hạng","Số lần","Chi tiêu","Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel);
        styleTable(); loadTable();

        // Selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean has = table.getSelectedRow() >= 0;
            btnSua.setEnabled(has); btnXoa.setEnabled(has);
        });
        // Double-click = edit
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount()==2) editSelected(); }
            @Override public void mousePressed(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r >= 0) table.setRowSelectionInterval(r, r);
            }
        });
        // Context menu
        table.setComponentPopupMenu(buildContextMenu());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        sp.setPreferredSize(new Dimension(0, 400));

        lblFooter = new JLabel();
        lblFooter.setFont(UIConstants.FONT_SMALL); lblFooter.setForeground(UIConstants.TEXT_MUTED);
        JLabel hint = new JLabel("Double-click để chỉnh sửa  ");
        hint.setFont(UIConstants.FONT_SMALL); hint.setForeground(UIConstants.TEXT_MUTED);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0, UIConstants.BORDER),
            BorderFactory.createEmptyBorder(6,12,6,12)));
        footer.add(lblFooter, BorderLayout.WEST); footer.add(hint, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        wrap.add(sp, BorderLayout.CENTER); wrap.add(footer, BorderLayout.SOUTH);
        return wrap;
    }

    private JPopupMenu buildContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem miXem  = new JMenuItem("◎ Xem / Sửa thông tin");
        JMenuItem miThem = new JMenuItem("+ Thêm khách hàng mới");
        JMenuItem miXoa  = new JMenuItem("✕ Xóa khách hàng này");
        miXem.setFont(UIConstants.FONT_BODY); miThem.setFont(UIConstants.FONT_BODY); miXoa.setFont(UIConstants.FONT_BODY);
        miXoa.setForeground(UIConstants.DANGER);
        miXem.addActionListener(e  -> editSelected());
        miThem.addActionListener(e -> showDialog(null));
        miXoa.addActionListener(e  -> deleteSelected());
        menu.add(miXem); menu.addSeparator(); menu.add(miThem); menu.addSeparator(); menu.add(miXoa);
        return menu;
    }

    private void styleTable() {
        table.setFont(UIConstants.FONT_BODY); table.setRowHeight(48);
        table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0,0));
        table.setBackground(Color.WHITE); table.setSelectionBackground(UIConstants.PRIMARY_LIGHT);
        table.getTableHeader().setFont(UIConstants.FONT_SMALL_BOLD);
        table.getTableHeader().setBackground(UIConstants.BG_TABLE_HEADER);
        table.getTableHeader().setForeground(UIConstants.TEXT_SECONDARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0, UIConstants.BORDER));
        table.getColumn("_ma").setMaxWidth(0); table.getColumn("_ma").setMinWidth(0);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean foc,int row,int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,val,sel,foc,row,col);
                lbl.setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
                lbl.setBackground(sel?UIConstants.PRIMARY_LIGHT:(row%2==0?Color.WHITE:new Color(0xFAFAFA)));
                lbl.setFont(UIConstants.FONT_BODY); lbl.setForeground(UIConstants.TEXT_PRIMARY);
                String v = val!=null?val.toString():"";
                if (col==5) { lbl.setForeground(UIConstants.getHangKhachColor(v)); lbl.setFont(UIConstants.FONT_SMALL_BOLD); }
                else if (col==7) { lbl.setFont(UIConstants.FONT_BODY_BOLD); lbl.setForeground(UIConstants.SUCCESS); }
                else if (col==8) { lbl.setForeground("Đang ở".equals(v)?UIConstants.SUCCESS:UIConstants.TEXT_MUTED); lbl.setFont(UIConstants.FONT_SMALL_BOLD); }
                return lbl;
            }
        });
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        String kw   = txtSearch!=null ? txtSearch.getText().trim() : "";
        String hang = "Tất cả".equals(currentHang) ? null : currentHang;
        List<KhachHang> list = service.search(kw.isEmpty()?null:kw, hang);
        for (KhachHang kh : list) {
            tableModel.addRow(new Object[]{
                kh.getMaKH(), kh.getHoTen(),
                kh.getSoDienThoai(), kh.getCccd()!=null?kh.getCccd():"",
                kh.getQuocTich()!=null?kh.getQuocTich():"Việt Nam", kh.getHang(),
                kh.getSoLanLuuTru()+" lần",
                String.format("%,.1f trd", kh.getTongChiTieu()/1_000_000.0),
                kh.getTrangThai()
            });
        }
        if (lblFooter!=null) lblFooter.setText("Hiển thị "+list.size()+" khách hàng");
        btnSua.setEnabled(false); btnXoa.setEnabled(false);
    }

    private KhachHang getSelected() {
        int row = table.getSelectedRow(); if (row<0) return null;
        return service.getById((String) tableModel.getValueAt(row, 0));
    }

    private void showDialog(KhachHang kh) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        KhachHangDialog dlg = new KhachHangDialog(owner, kh);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) refresh();
    }

    private void editSelected() {
        KhachHang kh = getSelected();
        if (kh==null) { JOptionPane.showMessageDialog(this,"Chọn một khách hàng để sửa!"); return; }
        showDialog(kh);
    }

    private void deleteSelected() {
        KhachHang kh = getSelected();
        if (kh==null) { JOptionPane.showMessageDialog(this,"Chọn một khách hàng để xóa!"); return; }
        if ("Đang ở".equals(kh.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                "Không thể xóa khách đang lưu trú!\nVui lòng trả phòng trước.",
                "Không thể xóa", JOptionPane.WARNING_MESSAGE); return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Xóa khách hàng: \"" + kh.getHoTen() + "\" (" + kh.getMaKH() + ")?\nHành động này không thể hoàn tác!",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            String err = service.xoa(kh.getMaKH());
            if (err==null) { JOptionPane.showMessageDialog(this,"Đã xóa thành công!"); refresh(); }
            else JOptionPane.showMessageDialog(this,"Lỗi: "+err,"Không thể xóa",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() { removeAll(); buildUI(); revalidate(); repaint(); }
}
