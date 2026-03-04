package com.hrm.gui.attendance;

import com.hrm.model.*;
import com.hrm.service.AttendanceService;
import com.hrm.service.AttendanceService.ServiceResult;
import com.hrm.service.MockDataService;
import com.hrm.util.SessionContext;
import com.hrm.util.UIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AttendancePanel extends JPanel {

    private final AttendanceService svc;
    private final User currentUser;
    private final boolean isAdmin;
    private final NumberFormat moneyFmt;

    private JTabbedPane tabbedPane;

    // Tab 1
    private JTable tableChamCong; private DefaultTableModel modelCC;
    private JComboBox<String> cboThang, cboNam;
    private JPanel statsPanel;
    // Tab 2
    private JTable tableCaLam; private DefaultTableModel modelCaLam;
    // Tab 3
    private JTable tableDonOT; private DefaultTableModel modelOT;
    // Tab 4
    private JTable tableLuong; private DefaultTableModel modelLuong;
    private JComboBox<String> cboThangL, cboNamL;
    private JPanel luongStats;
    private BangLuong bangLuongHienTai;
        // Thêm vào phần khai báo field (cùng chỗ với các field Tab 4 hiện có)
    private JSpinner spinnerThangRef;
    private JSpinner spinnerNamRef;
    // Tab 5
    private JTable tablePC; private DefaultTableModel modelPC;

    public AttendancePanel() {
        svc = AttendanceService.getInstance();
        currentUser = SessionContext.getInstance().getCurrentUser();
        isAdmin = SessionContext.getInstance().hasRole("ADMIN");
        moneyFmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        setLayout(new BorderLayout()); setBackground(UIColors.LIGHT_GRAY_BG);
        initTabs();
    }

    private void initTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(UIColors.WHITE);
        if (isAdmin) {
            tabbedPane.addTab("Tong hop cham cong", tabTongHop());
            tabbedPane.addTab("Quan ly ca lam", tabCaLam());
            tabbedPane.addTab("Duyet don OT", tabDuyetOT());
            tabbedPane.addTab("Phu cap & Khau tru", tabPhuCap());
            tabbedPane.addTab("Bang luong", tabBangLuong());
        } else {
            tabbedPane.addTab("Cham cong", tabCaNhan());
            tabbedPane.addTab("Lich su", tabTongHop());
        }
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════
    //  TAB 1 — TỔNG HỢP CHẤM CÔNG
    // ═══════════════════════════════════
    private JPanel tabTongHop() {
        JPanel p=panel();
        JPanel tb=new JPanel(new FlowLayout(FlowLayout.LEFT,10,5)); tb.setOpaque(false);
        tb.add(new JLabel("Thang:")); cboThang=combMonth(); tb.add(cboThang);
        tb.add(new JLabel("Nam:")); cboNam=combYear(); tb.add(cboNam);
        JButton bL=btn("Loc du lieu",UIColors.PRIMARY_PURPLE); bL.addActionListener(e->loadCC()); tb.add(bL);
        tb.add(Box.createHorizontalStrut(20));
        JButton bT=btn("+ Them thu cong",UIColors.SUCCESS_GREEN); bT.addActionListener(e->dlgThemCC()); tb.add(bT);
        p.add(tb,BorderLayout.NORTH);
        String[]cols={"Ma NV","Ho ten","Ngay","Ca lam","Gio vao","Gio ra","So gio","OT","Trang thai"};
        modelCC=mdl(cols); tableChamCong=tbl(modelCC);
        tableChamCong.getColumnModel().getColumn(8).setCellRenderer(new StatusR());
        p.add(new JScrollPane(tableChamCong),BorderLayout.CENTER);
        statsPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,30,10));
        statsPanel.setOpaque(false); statsPanel.setBorder(new EmptyBorder(5,0,0,0));
        p.add(statsPanel,BorderLayout.SOUTH); loadCC(); return p;
    }

    private void loadCC() {
        modelCC.setRowCount(0);
        int th = Integer.parseInt((String) cboThang.getSelectedItem());
        int nm = Integer.parseInt((String) cboNam.getSelectedItem());
        List<ChamCong> ds = svc.getChamCongTheoThang(th, nm);
        DateTimeFormatter fN = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter fG = DateTimeFormatter.ofPattern("HH:mm");
        for (ChamCong cc : ds) {
            // ✅ Lấy maNhanVien (mã hiển thị) từ DB thay vì getMaNV() (số int)
            String maNhanVien = com.hrm.repo.AttendanceRepository
                .getInstance().getMaNhanVienById(cc.getMaNV());
            modelCC.addRow(new Object[]{
                maNhanVien,
                cc.getEmployeeName() != null ? cc.getEmployeeName() : "NV-" + cc.getMaNV(),
                cc.getNgay().format(fN),
                cc.getTenCaLam() != null ? cc.getTenCaLam() : cc.getMaCaLam(),
                cc.getGioVao() != null ? cc.getGioVao().format(fG) : "-",
                cc.getGioRa()  != null ? cc.getGioRa().format(fG)  : "-",
                String.format("%.1f", cc.getSoGioLam()),
                cc.getGioLamThem() > 0 ? String.format("%.1f", cc.getGioLamThem()) : "-",
                cc.getTrangThai() != null ? cc.getTrangThai().getDisplayName() : "N/A"
            });
        }
        if (statsPanel == null) return;
        long dg = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.DUNG_GIO).count();
        long dm = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.DI_MUON).count();
        long vm = ds.stream().filter(c -> c.getTrangThai() == ChamCong.TrangThai.VANG_MAT).count();
        statsPanel.removeAll();
        statsPanel.add(lbl("Tong:", String.valueOf(ds.size()), UIColors.PRIMARY_PURPLE));
        statsPanel.add(lbl("Dung gio:", String.valueOf(dg), UIColors.SUCCESS_GREEN));
        statsPanel.add(lbl("Di muon:", String.valueOf(dm), UIColors.DANGER_RED));
        statsPanel.add(lbl("Vang:", String.valueOf(vm), Color.GRAY));
        statsPanel.revalidate();
        statsPanel.repaint();
    }    
    private void dlgThemCC() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Them cham cong thu cong", true);
        d.setSize(500, 560);
        d.setLocationRelativeTo(this);

        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = gbc();

        // ── Row 0: Mã NV + nút Tìm ──
        g.gridx = 0; g.gridy = 0; g.weightx = 0; g.gridwidth = 1;
        f.add(new JLabel("Ma nhan vien:"), g);
        JTextField txtMaNV = new JTextField(12);
        txtMaNV.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMaNV.setToolTipText("Nhap ma nhan vien (VD: NV001)");
        g.gridx = 1; g.weightx = 1;
        f.add(txtMaNV, g);
        JButton btnTim = btn("Tim", UIColors.PRIMARY_PURPLE);
        btnTim.setPreferredSize(new Dimension(70, 30));
        g.gridx = 2; g.weightx = 0;
        f.add(btnTim, g);

        // ── Row 1: Panel thông tin nhân viên (ẩn mặc định) ──
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        infoPanel.setBackground(new Color(245, 247, 255));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIColors.PRIMARY_PURPLE, 1),
            new EmptyBorder(10, 12, 10, 12)));
        infoPanel.setVisible(false);

        JLabel lblHoTenVal     = new JLabel(""); lblHoTenVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblEmailVal     = new JLabel(""); lblEmailVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel lblChucVuVal    = new JLabel(""); lblChucVuVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel lblPhongBanVal  = new JLabel(""); lblPhongBanVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel lblTrangThaiVal = new JLabel(""); lblTrangThaiVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        infoPanel.add(boldLabel("Ho ten:"));    infoPanel.add(lblHoTenVal);
        infoPanel.add(boldLabel("Email:"));     infoPanel.add(lblEmailVal);
        infoPanel.add(boldLabel("Chuc vu:"));   infoPanel.add(lblChucVuVal);
        infoPanel.add(boldLabel("Phong ban:")); infoPanel.add(lblPhongBanVal);
        infoPanel.add(boldLabel("Trang thai:")); infoPanel.add(lblTrangThaiVal);

        g.gridx = 0; g.gridy = 1; g.gridwidth = 3;
        g.insets = new Insets(8, 8, 8, 8);
        f.add(infoPanel, g);
        g.gridwidth = 1; g.insets = new Insets(8, 8, 8, 8);

        // Mảng giữ maNV tìm được
        int[] maNVRef = {-1};

        // ── Hành động Tìm ──
        btnTim.addActionListener(e -> {
            String ma = txtMaNV.getText().trim();
            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Vui long nhap ma nhan vien.", "Thong bao", JOptionPane.WARNING_MESSAGE);
                return;
            }
            com.hrm.repo.AttendanceRepository.NhanVienInfo info =
                com.hrm.repo.AttendanceRepository.getInstance().findNhanVienByMa(ma);
            if (info == null) {
                infoPanel.setVisible(false);
                maNVRef[0] = -1;
                JOptionPane.showMessageDialog(d, "Khong tim thay nhan vien: " + ma, "Loi", JOptionPane.ERROR_MESSAGE);
            } else {
                maNVRef[0] = info.maNV;
                lblHoTenVal.setText(info.hoTen);
                lblEmailVal.setText(info.email.isEmpty() ? "(Chua co)" : info.email);
                lblChucVuVal.setText(info.tenChucVu.isEmpty() ? "(Chua co)" : info.tenChucVu);
                lblPhongBanVal.setText(info.tenPhongBan.isEmpty() ? "(Chua co)" : info.tenPhongBan);
                lblTrangThaiVal.setText(formatTrangThaiNV(info.trangThai));
                lblTrangThaiVal.setForeground(
                    "dang_lam_viec".equals(info.trangThai) ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
                infoPanel.setVisible(true);
                d.revalidate(); d.repaint();
            }
        });
        txtMaNV.addActionListener(e -> btnTim.doClick()); // Enter = Tìm

        // ── Row 2: Ngày ──
        g.gridx = 0; g.gridy = 2; g.weightx = 0; g.gridwidth = 1;
        f.add(new JLabel("Ngay (dd/MM/yyyy):"), g);
        JTextField txtNgay = new JTextField(
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(txtNgay, g);

        // ── Row 3: Ca làm ──
        g.gridx = 0; g.gridy = 3; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Ca lam:"), g);
        JComboBox<String> cCa = new JComboBox<>();
        for (CaLam ca : svc.getDanhSachCaLam()) cCa.addItem(ca.getMaCaLam() + " - " + ca.getTenCaLam());
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(cCa, g);

        // ── Row 4: Giờ vào ──
        g.gridx = 0; g.gridy = 4; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Gio vao (HH:mm):"), g);
        JTextField tV = new JTextField("08:00");
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(tV, g);

        // ── Row 5: Giờ ra ──
        g.gridx = 0; g.gridy = 5; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Gio ra (HH:mm):"), g);
        JTextField tR = new JTextField("17:00");
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(tR, g);

        // ── Row 6: Trạng thái ──
        g.gridx = 0; g.gridy = 6; g.gridwidth = 1; g.weightx = 0;
        f.add(new JLabel("Trang thai:"), g);
        JComboBox<String> cTT = new JComboBox<>(
            new String[]{"Dung gio", "Di muon", "Ve som", "Vang mat"});
        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        f.add(cTT, g);

        // ── Row 7: Nút Lưu ──
        JButton bLuu = btn("Luu", UIColors.SUCCESS_GREEN);
        bLuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bLuu.setPreferredSize(new Dimension(120, 38));
        g.gridx = 0; g.gridy = 7; g.gridwidth = 3;
        g.insets = new Insets(20, 8, 8, 8);
        f.add(bLuu, g);

        ChamCong.TrangThai[] tts = {
            ChamCong.TrangThai.DUNG_GIO, ChamCong.TrangThai.DI_MUON,
            ChamCong.TrangThai.VE_SOM,   ChamCong.TrangThai.VANG_MAT
        };

        bLuu.addActionListener(e -> {
            try {
                if (maNVRef[0] == -1) {
                    JOptionPane.showMessageDialog(d,
                        "Vui long tim kiem nhan vien truoc khi luu.",
                        "Thieu thong tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                LocalDate ngay;
                try {
                    ngay = LocalDate.parse(txtNgay.getText().trim(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(d,
                        "Dinh dang ngay khong hop le. Dung: dd/MM/yyyy",
                        "Loi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String maCa = ((String) cCa.getSelectedItem()).split(" - ")[0].trim();
                String[] v  = tV.getText().trim().split(":");
                String[] r  = tR.getText().trim().split(":");

                ChamCong cc = new ChamCong();
                cc.setMaNV(maNVRef[0]);
                cc.setNgay(ngay);
                cc.setMaCaLam(maCa);
                CaLam caL = svc.getDanhSachCaLam().stream()
                    .filter(c -> c.getMaCaLam().equals(maCa)).findFirst().orElse(null);
                cc.setTenCaLam(caL != null ? caL.getTenCaLam() : maCa);
                cc.setGioVao(ngay.atTime(Integer.parseInt(v[0]), Integer.parseInt(v[1])));
                cc.setGioRa(ngay.atTime(Integer.parseInt(r[0]), Integer.parseInt(r[1])));
                cc.setSoGioLam(cc.tinhSoGioLam());
                cc.setPhuongThucChamCong(ChamCong.PhuongThuc.THU_CONG);
                cc.setEmployeeName(lblHoTenVal.getText());
                cc.setTrangThai(tts[cTT.getSelectedIndex()]);
                if (caL != null && cc.getSoGioLam() > caL.getSoGioChuan())
                    cc.setGioLamThem(cc.getSoGioLam() - caL.getSoGioChuan());

                com.hrm.repo.AttendanceRepository.getInstance().saveChamCong(cc);
                JOptionPane.showMessageDialog(d,
                    "Da them cham cong cho: " + lblHoTenVal.getText(),
                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
                loadCC();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Loi: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
            }
        });

        d.setContentPane(f);
        d.setVisible(true);
    }



    /** Chuyển trangThai DB → text hiển thị */
    private String formatTrangThaiNV(String tt) {
        return switch (tt) {
            case "dang_lam_viec" -> "Dang lam viec";
            case "tam_nghi"      -> "Tam nghi";
            case "nghi_viec"     -> "Nghi viec";
            default              -> tt;
        };
    }    
    /** Helper tạo JLabel in đậm */
    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return l;
    }

    /** Chuyển trangThai DB → text hiển thị */
    // ═══════════════════════════════════
    //  TAB 2 — QUẢN LÝ CA LÀM
    // ═══════════════════════════════════
    private JPanel tabCaLam() {
        JPanel p=panel();JPanel hdr=new JPanel(new BorderLayout());hdr.setOpaque(false);
        JLabel l=new JLabel("Danh sach ca lam viec");l.setFont(new Font("Segoe UI",Font.BOLD,16));l.setForeground(UIColors.TEXT_DARK);
        hdr.add(l,BorderLayout.WEST);
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));bp.setOpaque(false);
        JButton bT=btn("+ Them ca moi",UIColors.SUCCESS_GREEN);bT.addActionListener(e->dlgCaLam(null));
        JButton bS=btn("Sua",UIColors.PRIMARY_PURPLE);bS.addActionListener(e->{
            int row=tableCaLam.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Chon ca lam.");return;}
            CaLam ca=com.hrm.repo.AttendanceRepository.getInstance().findCaLamByMa((String)modelCaLam.getValueAt(row,0));
            if(ca!=null)dlgCaLam(ca);});
        JButton bX=btn("Xoa",UIColors.DANGER_RED);bX.addActionListener(e->xoaCaLam());
        bp.add(bT);bp.add(bS);bp.add(bX);hdr.add(bp,BorderLayout.EAST);p.add(hdr,BorderLayout.NORTH);
        String[]cols={"Ma ca","Ten ca","Gio bat dau","Gio ket thuc","So gio chuan","Cho phep OT","Trang thai"};
        modelCaLam=mdl(cols);tableCaLam=tbl(modelCaLam);
        tableCaLam.getColumnModel().getColumn(6).setCellRenderer(new StatusR());
        loadCaLam();p.add(new JScrollPane(tableCaLam),BorderLayout.CENTER);return p;
    }
    private void loadCaLam(){modelCaLam.setRowCount(0);DateTimeFormatter f=DateTimeFormatter.ofPattern("HH:mm");
        for(CaLam ca:svc.getTatCaCaLam())modelCaLam.addRow(new Object[]{ca.getMaCaLam(),ca.getTenCaLam(),
            ca.getGioBatDau().format(f),ca.getGioKetThuc().format(f),ca.getSoGioChuan(),
            ca.isChoPhepLamThem()?"Co":"Khong",ca.conHoatDong()?"Hoat dong":"Ngung hoat dong"});}
    private void dlgCaLam(CaLam ex){boolean edit=ex!=null;
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),edit?"Sua ca":"Them ca moi",true);
        d.setSize(450,380);d.setLocationRelativeTo(this);
        JPanel f=new JPanel(new GridBagLayout());f.setBorder(new EmptyBorder(20,20,20,20));
        GridBagConstraints g=gbc();DateTimeFormatter fmt=DateTimeFormatter.ofPattern("HH:mm");
        g.gridx=0;g.gridy=0;f.add(new JLabel("Ma ca:"),g);
        JTextField tMa=new JTextField(edit?ex.getMaCaLam():"",20);tMa.setEditable(!edit);
        if(edit)tMa.setBackground(new Color(240,240,240));g.gridx=1;g.weightx=1;f.add(tMa,g);
        g.gridx=0;g.gridy=1;g.weightx=0;f.add(new JLabel("Ten ca:"),g);
        JTextField tTen=new JTextField(edit?ex.getTenCaLam():"");g.gridx=1;f.add(tTen,g);
        g.gridx=0;g.gridy=2;f.add(new JLabel("Gio bat dau:"),g);
        JTextField tBD=new JTextField(edit?ex.getGioBatDau().format(fmt):"08:00");g.gridx=1;f.add(tBD,g);
        g.gridx=0;g.gridy=3;f.add(new JLabel("Gio ket thuc:"),g);
        JTextField tKT=new JTextField(edit?ex.getGioKetThuc().format(fmt):"17:00");g.gridx=1;f.add(tKT,g);
        g.gridx=0;g.gridy=4;f.add(new JLabel("So gio chuan:"),g);
        JTextField tGio=new JTextField(edit?String.valueOf(ex.getSoGioChuan()):"8.0");g.gridx=1;f.add(tGio,g);
        g.gridx=0;g.gridy=5;f.add(new JLabel("Cho phep OT:"),g);
        JCheckBox chk=new JCheckBox("Co",!edit||ex.isChoPhepLamThem());chk.setOpaque(false);g.gridx=1;f.add(chk,g);
        JButton bL=btn(edit?"Cap nhat":"Them moi",UIColors.PRIMARY_PURPLE);bL.setFont(new Font("Segoe UI",Font.BOLD,14));
        bL.addActionListener(e->{try{String ma=tMa.getText().trim(),ten=tTen.getText().trim();
            if(ma.isEmpty()||ten.isEmpty()){JOptionPane.showMessageDialog(d,"Khong de trong.");return;}
            double sg=Double.parseDouble(tGio.getText().trim());
            ServiceResult<CaLam>res=edit?svc.suaCaLam(ma,ten,tBD.getText().trim(),tKT.getText().trim(),sg,chk.isSelected())
                :svc.themCaLam(ma,ten,tBD.getText().trim(),tKT.getText().trim(),sg,chk.isSelected());
            if(res.isSuccess()){JOptionPane.showMessageDialog(d,res.getMessage());d.dispose();loadCaLam();}
            else JOptionPane.showMessageDialog(d,res.getMessage(),"Loi",JOptionPane.ERROR_MESSAGE);
        }catch(NumberFormatException x){JOptionPane.showMessageDialog(d,"So gio phai la so.");}
         catch(Exception x){JOptionPane.showMessageDialog(d,"Loi: "+x.getMessage());}});
        g.gridx=0;g.gridy=6;g.gridwidth=2;g.insets=new Insets(20,8,8,8);f.add(bL,g);
        d.setContentPane(f);d.setVisible(true);}
    private void xoaCaLam(){int row=tableCaLam.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Chon ca.");return;}
        String ma=(String)modelCaLam.getValueAt(row,0),ten=(String)modelCaLam.getValueAt(row,1);
        if(((String)modelCaLam.getValueAt(row,6)).contains("Ngung")){JOptionPane.showMessageDialog(this,"Da ngung.");return;}
        if(JOptionPane.showConfirmDialog(this,"Ngung '"+ten+"'?","Xac nhan",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            svc.xoaCaLam(ma);loadCaLam();}}

    // ═══════════════════════════════════
    //  TAB 3 — DUYỆT ĐƠN OT
    // ═══════════════════════════════════
    private JPanel tabDuyetOT(){JPanel p=panel();JPanel hdr=new JPanel(new BorderLayout());hdr.setOpaque(false);
        JLabel l=new JLabel("Don dang ky lam them gio");l.setFont(new Font("Segoe UI",Font.BOLD,16));l.setForeground(UIColors.TEXT_DARK);
        hdr.add(l,BorderLayout.WEST);
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));bp.setOpaque(false);
        JButton bD=btn("Duyet",UIColors.SUCCESS_GREEN);bD.addActionListener(e->duyetOT());
        JButton bTC=btn("Tu choi",UIColors.DANGER_RED);bTC.addActionListener(e->tuChoiOT());
        JButton bHS=btn("Chinh he so",UIColors.PRIMARY_PURPLE);bHS.addActionListener(e->chinhHeSo());
        JButton bR=new JButton("Lam moi");bR.setFocusPainted(false);bR.addActionListener(e->loadOT());
        bp.add(bD);bp.add(bTC);bp.add(bHS);bp.add(bR);hdr.add(bp,BorderLayout.EAST);p.add(hdr,BorderLayout.NORTH);
        String[]cols={"Ma DK","Ma NV","Ngay","So gio","He so OT","Ly do","Trang thai","Nguoi duyet"};
        modelOT=mdl(cols);tableDonOT=tbl(modelOT);
        tableDonOT.getColumnModel().getColumn(6).setCellRenderer(new StatusR());
        tableDonOT.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                super.getTableCellRendererComponent(t,v,s,f,r,c);setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI",Font.BOLD,13));String str=v!=null?v.toString():"";
                setForeground(str.contains("2.0")||str.contains("3.0")?UIColors.DANGER_RED:UIColors.PRIMARY_PURPLE);
                return this;}});
        loadOT();p.add(new JScrollPane(tableDonOT),BorderLayout.CENTER);
        JPanel info=new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));info.setOpaque(false);
        info.setBorder(new EmptyBorder(5,0,0,0));
        info.add(lbl("He so:","x1.5 (thuong), x2.0 (cuoi tuan), x3.0 (le)",UIColors.TEXT_DARK));
        p.add(info,BorderLayout.SOUTH);return p;}
    private void loadOT() {
        modelOT.setRowCount(0);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DangKyLamThem don : com.hrm.repo.AttendanceRepository.getInstance().findAllDonOT()) {
            // ✅ Hiển thị maNhanVien (VD: NV001) thay vì số int
            String maNhanVien = com.hrm.repo.AttendanceRepository
                .getInstance().getMaNhanVienById(don.getMaNV());
            String tenNV = don.getEmployeeName() != null
                ? don.getEmployeeName() : maNhanVien;
            modelOT.addRow(new Object[]{
                don.getMaDK(),
                maNhanVien + " - " + tenNV,
                don.getNgay() != null ? don.getNgay().format(f) : "-",
                String.format("%.1f", don.getSoGio()),
                "x" + don.getHeSoOT(),
                don.getLyDo(),
                don.getTrangThai().getDisplayName(),
                don.getApproverName() != null ? don.getApproverName() : "-"
            });
        }
    }                
    private void duyetOT(){int row=tableDonOT.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Chon don.");return;}
        int maDK=(int)modelOT.getValueAt(row,0);String tt=(String)modelOT.getValueAt(row,6);
        if(!tt.contains("Cho")){JOptionPane.showMessageDialog(this,"Don da xu ly.");return;}
        String[]opts={"x1.5 (ngay thuong)","x2.0 (cuoi tuan)","x3.0 (ngay le)"};
        String ch=(String)JOptionPane.showInputDialog(this,"Chon he so OT:","He so OT",
            JOptionPane.QUESTION_MESSAGE,null,opts,opts[0]);if(ch==null)return;
        double hs=ch.contains("2.0")?2.0:ch.contains("3.0")?3.0:1.5;
        ServiceResult<?>res=svc.duyetDonLamThem(maDK,currentUser.getId(),hs);
        JOptionPane.showMessageDialog(this,res.getMessage());loadOT();}
    private void tuChoiOT(){int row=tableDonOT.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Chon don.");return;}
        int maDK=(int)modelOT.getValueAt(row,0);String tt=(String)modelOT.getValueAt(row,6);
        if(!tt.contains("Cho")){JOptionPane.showMessageDialog(this,"Don da xu ly.");return;}
        svc.tuChoiDonLamThem(maDK,currentUser.getId());loadOT();}
    private void chinhHeSo(){int row=tableDonOT.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Chon don.");return;}
        int maDK=(int)modelOT.getValueAt(row,0);
        String input=JOptionPane.showInputDialog(this,"Nhap he so moi (vd: 1.5):","Chinh he so",JOptionPane.QUESTION_MESSAGE);
        if(input==null||input.trim().isEmpty())return;
        try{double hs=Double.parseDouble(input.trim());svc.capNhatHeSoOT(maDK,hs);loadOT();
        }catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"He so phai la so.");}}

    // ═══════════════════════════════════
    //  TAB 4 — BẢNG LƯƠNG
    // ═══════════════════════════════════
    // ═══════════════════════════════════
    //  TAB BẢNG LƯƠNG
    // ═══════════════════════════════════
    private JPanel tabBangLuong() {
        JPanel p = panel();

        // ── Toolbar: chọn tháng/năm cải tiến ──
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        tb.setOpaque(false);

        // Tháng: spinner thay vì combobox string
        tb.add(new JLabel("Thang:"));
        SpinnerNumberModel smThang = new SpinnerNumberModel(
            LocalDate.now().getMonthValue(), 1, 12, 1);
        JSpinner spinThang = new JSpinner(smThang);
        spinThang.setPreferredSize(new Dimension(60, 30));
        ((JSpinner.DefaultEditor) spinThang.getEditor()).getTextField()
            .setHorizontalAlignment(JTextField.CENTER);
        tb.add(spinThang);

        // Năm: spinner từ 2020 đến năm hiện tại + 1
        tb.add(new JLabel("Nam:"));
        SpinnerNumberModel smNam = new SpinnerNumberModel(
            LocalDate.now().getYear(), 2020, LocalDate.now().getYear() + 1, 1);
        JSpinner spinNam = new JSpinner(smNam);
        spinNam.setPreferredSize(new Dimension(80, 30));
        ((JSpinner.DefaultEditor) spinNam.getEditor()).getTextField()
            .setHorizontalAlignment(JTextField.CENTER);
        tb.add(spinNam);

        // Label hiển thị tháng/năm đang xem
        JLabel lblThangNam = new JLabel();
        lblThangNam.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThangNam.setForeground(UIColors.PRIMARY_PURPLE);
        Runnable capNhatLabel = () -> lblThangNam.setText(
            String.format("Thang %02d/%d",
                (int) spinThang.getValue(), (int) spinNam.getValue()));
        capNhatLabel.run();
        spinThang.addChangeListener(e -> capNhatLabel.run());
        spinNam.addChangeListener(e -> capNhatLabel.run());
        tb.add(lblThangNam);

        tb.add(Box.createHorizontalStrut(10));
        JButton bTinh = btn("Tinh / Lam moi luong", UIColors.PRIMARY_PURPLE);
        tb.add(bTinh);
        tb.add(Box.createHorizontalStrut(10));
        JButton bCT = btn("Xem chi tiet", UIColors.INFO_BLUE);
        tb.add(bCT);

        p.add(tb, BorderLayout.NORTH);

        // ── Bảng ──
        String[] cols = {"Ma NV", "Ho ten", "Luong co ban", "Ngay cong",
            "Gio lam", "Gio OT", "Phu cap", "Khau tru",
            "Tien OT", "Tong luong", "Thuc nhan", "Trang thai"};
        modelLuong = mdl(cols);
        tableLuong = tbl(modelLuong);

        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(RIGHT); setForeground(Color.BLACK); return this;
            }
        };
        for (int i : new int[]{2, 6, 7, 8, 9}) tableLuong.getColumnModel().getColumn(i).setCellRenderer(rightR);
        tableLuong.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(UIColors.PRIMARY_PURPLE); return this;
            }
        });
        tableLuong.getColumnModel().getColumn(11).setCellRenderer(new StatusR());
        p.add(new JScrollPane(tableLuong), BorderLayout.CENTER);

        luongStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        luongStats.setOpaque(false);
        luongStats.setBorder(new EmptyBorder(5, 0, 0, 0));
        p.add(luongStats, BorderLayout.SOUTH);

        // ── Sự kiện ──
        bTinh.addActionListener(e -> loadLuongFromSpinner(spinThang, spinNam));
        bCT.addActionListener(e -> xemChiTiet());

        // Load tháng hiện tại lúc khởi động
        loadLuongFromSpinner(spinThang, spinNam);
        return p;
    }

    /** Lấy thang/nam từ spinner và gọi loadLuong */
    private void loadLuongFromSpinner(JSpinner spinThang, JSpinner spinNam) {
        // Lưu lại spinner reference để xemChiTiet() dùng được
        spinnerThangRef = spinThang;
        spinnerNamRef   = spinNam;
        int th = (int) spinThang.getValue();
        int nm = (int) spinNam.getValue();
        loadLuong(th, nm);
    }

    private void loadLuong(int th, int nm) {
        modelLuong.setRowCount(0);
        bangLuongHienTai = svc.tinhBangLuong(th, nm);
        List<ChiTietLuong> ds = svc.getChiTietLuong(bangLuongHienTai.getMaBL());

        if (ds.isEmpty()) {
            if (luongStats != null) {
                luongStats.removeAll();
                luongStats.add(lbl("Thang " + th + "/" + nm + ":",
                    "Chua co du lieu cham cong", Color.GRAY));
                luongStats.revalidate(); luongStats.repaint();
            }
            return;
        }

        double tQ = 0, tO = 0, tK = 0;
        for (ChiTietLuong ct : ds) {
            // ✅ Hiển thị maNhanVien thay vì số int
            String maNhanVien = com.hrm.repo.AttendanceRepository
                .getInstance().getMaNhanVienById(ct.getMaNV());
            modelLuong.addRow(new Object[]{
                maNhanVien,
                ct.getTenNV(),
                fmtTien(ct.getLuongCoBan()),
                ct.getSoNgayCong(),
                String.format("%.1f", ct.getTongGioLam()),
                ct.getTongGioOT() > 0 ? String.format("%.1f", ct.getTongGioOT()) : "-",
                fmtTien(ct.getTongLuongChucVu()),
                fmtTien(ct.getTongKhauTru()),
                ct.getTienOT() > 0 ? fmtTien(ct.getTienOT()) : "-",
                fmtTien(ct.getTongLuong()),
                fmtTien(ct.getLuongThucNhan()),
                ct.getTrangThai().getDisplayName()
            });
            tQ += ct.getLuongThucNhan();
            tO += ct.getTienOT();
            tK += ct.getTongKhauTru();
        }

        if (luongStats == null) return;
        luongStats.removeAll();
        luongStats.add(lbl("Nhan vien:", String.valueOf(ds.size()), UIColors.PRIMARY_PURPLE));
        luongStats.add(lbl("Tong quy luong:", fmtTien(tQ), UIColors.SUCCESS_GREEN));
        luongStats.add(lbl("Tong tien OT:", fmtTien(tO), UIColors.INFO_BLUE));
        luongStats.add(lbl("Tong khau tru:", fmtTien(tK), UIColors.DANGER_RED));
        luongStats.revalidate(); luongStats.repaint();
    } 

    private void xemChiTiet() {
        int row = tableLuong.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chon nhan vien de xem."); return; }
        if (bangLuongHienTai == null) { JOptionPane.showMessageDialog(this, "Chua tinh luong."); return; }

        // Cột 0 giờ là maNhanVien (String), cột 1 là tên
        String maNhanVienHienThi = (String) modelLuong.getValueAt(row, 0);
        String tenNV = (String) modelLuong.getValueAt(row, 1);

        List<ChiTietLuong> dsCT = svc.getChiTietLuong(bangLuongHienTai.getMaBL());
        // Tìm ChiTietLuong khớp với maNhanVien đang hiển thị
        ChiTietLuong ct = dsCT.stream()
            .filter(c -> com.hrm.repo.AttendanceRepository.getInstance()
                .getMaNhanVienById(c.getMaNV()).equals(maNhanVienHienThi))
            .findFirst().orElse(null);

        if (ct == null) { JOptionPane.showMessageDialog(this, "Khong tim thay chi tiet."); return; }

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Chi tiet luong: " + tenNV, true);
        d.setSize(650, 500);
        d.setLocationRelativeTo(this);

        // ── Thông tin tổng hợp ──
        JPanel info = new JPanel(new GridLayout(0, 2, 10, 8));
        info.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Tong hop"),
            new EmptyBorder(10, 10, 10, 10)));

        info.add(boldLabel("Nhan vien:")); info.add(new JLabel(tenNV));
        info.add(boldLabel("Ma NV:"));     info.add(new JLabel(maNhanVienHienThi));
        info.add(boldLabel("Ngay cong:")); info.add(new JLabel(String.valueOf((int) ct.getSoNgayCong())));
        info.add(boldLabel("Gio lam:"));   info.add(new JLabel(String.format("%.1f", ct.getTongGioLam())));
        info.add(boldLabel("Gio OT:"));    info.add(new JLabel(String.format("%.1f", ct.getTongGioOT())));
        info.add(boldLabel("Luong chinh:")); info.add(new JLabel(fmtTien(ct.getLuongCoBan())));
        info.add(boldLabel("Tien OT:"));   info.add(new JLabel(fmtTien(ct.getTienOT())));
        info.add(boldLabel("Phu cap:"));   info.add(new JLabel(fmtTien(ct.getTongLuongChucVu())));
        info.add(boldLabel("Khau tru:"));  info.add(new JLabel(fmtTien(ct.getTongKhauTru())));
        info.add(boldLabel("Tong thu nhap:")); info.add(new JLabel(fmtTien(ct.getTongLuong())));
        JLabel lblThucNhan = new JLabel(fmtTien(ct.getLuongThucNhan()));
        lblThucNhan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblThucNhan.setForeground(UIColors.PRIMARY_PURPLE);
        info.add(boldLabel("THUC NHAN:")); info.add(lblThucNhan);

        // ── Bảng thành phần ──
        String[] cols = {"Loai", "Ten khoan", "So tien"};
        DefaultTableModel mTP = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (ThanhPhanLuong tp : ct.getDanhSachThanhPhan()) {
            mTP.addRow(new Object[]{
                tp.getLoai().getDisplayName(), tp.getTenKhoan(), fmtTien(tp.getSoTien())
            });
        }
        JTable tbl = new JTable(mTP);
        tbl.setRowHeight(28);
        tbl.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        tbl.getTableHeader().setForeground(Color.WHITE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));
        main.add(info, BorderLayout.NORTH);
        main.add(new JScrollPane(tbl), BorderLayout.CENTER);

        JButton btnDong = btn("Dong", UIColors.PRIMARY_PURPLE);
        btnDong.addActionListener(e -> d.dispose());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnP.add(btnDong);
        main.add(btnP, BorderLayout.SOUTH);

        d.setContentPane(main);
        d.setVisible(true);
    }        
    // ═══════════════════════════════════════════════
    //  TAB 5 — QUẢN LÝ PHỤ CẤP & KHẤU TRỪ
    // ═══════════════════════════════════════════════

    private JPanel tabPhuCap() {
        JPanel p = panel();

        // Header
        JPanel hdr = new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JLabel l = new JLabel("Cau hinh phu cap & khau tru");
        l.setFont(new Font("Segoe UI", Font.BOLD, 16)); l.setForeground(UIColors.TEXT_DARK);
        hdr.add(l, BorderLayout.WEST);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); bp.setOpaque(false);
        JButton bThem = btn("+ Them khoan moi", UIColors.SUCCESS_GREEN);
        bThem.addActionListener(e -> dlgPhuCap(null));
        JButton bSua = btn("Sua", UIColors.PRIMARY_PURPLE);
        bSua.addActionListener(e -> {
            int row = tablePC.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Chon khoan de sua."); return; }
            int maPC = (int) modelPC.getValueAt(row, 0);
            CauHinhPhuCap pc = com.hrm.repo.AttendanceRepository.getInstance().findCauHinhPCById(maPC);
            if (pc != null) dlgPhuCap(pc);
        });
        JButton bXoa = btn("Xoa", UIColors.DANGER_RED);
        bXoa.addActionListener(e -> xoaPhuCap());
        bp.add(bThem); bp.add(bSua); bp.add(bXoa);
        hdr.add(bp, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        // Bảng
        String[] cols = {"Ma", "Loai", "Ten khoan", "Kieu tinh", "Gia tri", "Nguon", "Trang thai"};
        modelPC = mdl(cols);
        tablePC = tbl(modelPC);

        // Renderer loại
        tablePC.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                String str = v != null ? v.toString() : "";
                setForeground(str.contains("cap") ? UIColors.SUCCESS_GREEN : UIColors.DANGER_RED);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setHorizontalAlignment(CENTER); return this;
            }
        });

        // Renderer giá trị — align phải
        tablePC.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(RIGHT); setForeground(Color.BLACK);
                setFont(new Font("Segoe UI", Font.BOLD, 13)); return this;
            }
        });

        // Renderer trạng thái
        tablePC.getColumnModel().getColumn(6).setCellRenderer(new StatusR());

        loadPhuCap();
        p.add(new JScrollPane(tablePC), BorderLayout.CENTER);

        // Info panel
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5)); info.setOpaque(false);
        info.setBorder(new EmptyBorder(5, 0, 0, 0));
        info.add(lbl("Kieu tinh:", "'Co dinh' = so tien cu the, '% Luong CB' = phan tram luong co ban", UIColors.TEXT_GRAY));
        p.add(info, BorderLayout.SOUTH);

        return p;
    }

    private void loadPhuCap() {
        modelPC.setRowCount(0);
        for (CauHinhPhuCap pc : svc.getAllCauHinhPC()) {
            String giaTri;
            if (pc.getKieuTinh() == CauHinhPhuCap.KieuTinh.PHAN_TRAM) {
                giaTri = pc.hienThiGiaTri();
            } else {
                giaTri = fmtTien(pc.getGiaTri());
            }
            modelPC.addRow(new Object[]{
                pc.getMaPC(),
                pc.getLoai().getDisplayName(),
                pc.getTenKhoan(),
                pc.getKieuTinh().getDisplayName(),
                giaTri,
                pc.getNguon(),
                pc.isHoatDong() ? "Hoat dong" : "Ngung"
            });
        }
    }

    private void dlgPhuCap(CauHinhPhuCap existing) {
        boolean edit = (existing != null);
        String title = edit ? "Sua: " + existing.getTenKhoan() : "Them khoan moi";

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        d.setSize(500, 400); d.setLocationRelativeTo(this);

        JPanel f = new JPanel(new GridBagLayout());
        f.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = gbc();

        // Loại
        g.gridx = 0; g.gridy = 0;
        f.add(new JLabel("Loai:"), g);
        JComboBox<String> cboLoai = new JComboBox<>(new String[]{"Phu cap", "Khau tru"});
        if (edit) cboLoai.setSelectedIndex(existing.getLoai() == ThanhPhanLuong.Loai.PHU_CAP ? 0 : 1);
        g.gridx = 1; g.weightx = 1; f.add(cboLoai, g);

        // Tên khoản
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        f.add(new JLabel("Ten khoan:"), g);
        JTextField tTen = new JTextField(edit ? existing.getTenKhoan() : "", 25);
        g.gridx = 1; g.weightx = 1; f.add(tTen, g);

        // Kiểu tính
        g.gridx = 0; g.gridy = 2; g.weightx = 0;
        f.add(new JLabel("Kieu tinh:"), g);
        JComboBox<String> cboKieu = new JComboBox<>(new String[]{"Co dinh (so tien)", "% Luong co ban"});
        if (edit) cboKieu.setSelectedIndex(existing.getKieuTinh() == CauHinhPhuCap.KieuTinh.CO_DINH ? 0 : 1);
        g.gridx = 1; f.add(cboKieu, g);

        // Giá trị
        g.gridx = 0; g.gridy = 3; g.weightx = 0;
        f.add(new JLabel("Gia tri:"), g);
        JTextField tGiaTri = new JTextField(edit ? String.valueOf(existing.getGiaTri()) : "");
        g.gridx = 1; f.add(tGiaTri, g);

        // Hint giá trị
        g.gridx = 1; g.gridy = 4;
        JLabel lblHint = new JLabel("VD: 500000 (co dinh) hoac 8 (phan tram)");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(UIColors.TEXT_LIGHT_GRAY);
        f.add(lblHint, g);

        // Cập nhật hint theo kiểu tính
        cboKieu.addActionListener(e -> {
            if (cboKieu.getSelectedIndex() == 0) {
                lblHint.setText("VD: 500000 (dong), 1000000 (dong)");
            } else {
                lblHint.setText("VD: 8 (= 8% luong co ban), 1.5 (= 1.5%)");
            }
        });

        // Nguồn
        g.gridx = 0; g.gridy = 5; g.weightx = 0;
        f.add(new JLabel("Nguon:"), g);
        JComboBox<String> cboNguon = new JComboBox<>(new String[]{"CongTy", "LuatDinh", "ChucVu", "Khac"});
        if (edit) cboNguon.setSelectedItem(existing.getNguon());
        g.gridx = 1; f.add(cboNguon, g);

        // Nút lưu
        JButton bLuu = btn(edit ? "Cap nhat" : "Them moi", UIColors.PRIMARY_PURPLE);
        bLuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bLuu.addActionListener(e -> {
            try {
                String ten = tTen.getText().trim();
                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(d, "Ten khoan khong duoc de trong.", "Loi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double giaTri = Double.parseDouble(tGiaTri.getText().trim());
                if (giaTri <= 0) {
                    JOptionPane.showMessageDialog(d, "Gia tri phai lon hon 0.", "Loi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ThanhPhanLuong.Loai loai = cboLoai.getSelectedIndex() == 0
                        ? ThanhPhanLuong.Loai.PHU_CAP : ThanhPhanLuong.Loai.KHAU_TRU;
                CauHinhPhuCap.KieuTinh kieu = cboKieu.getSelectedIndex() == 0
                        ? CauHinhPhuCap.KieuTinh.CO_DINH : CauHinhPhuCap.KieuTinh.PHAN_TRAM;
                String nguon = (String) cboNguon.getSelectedItem();

                ServiceResult<?> res;
                if (edit) {
                    res = svc.suaCauHinhPC(existing.getMaPC(), loai, ten, kieu, giaTri, nguon);
                } else {
                    res = svc.themCauHinhPC(loai, ten, kieu, giaTri, nguon);
                }

                if (res.isSuccess()) {
                    JOptionPane.showMessageDialog(d, res.getMessage(), "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                    d.dispose(); loadPhuCap();
                } else {
                    JOptionPane.showMessageDialog(d, res.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Gia tri phai la so.\nVD: 500000 hoac 8", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        });

        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.insets = new Insets(20, 8, 8, 8);
        f.add(bLuu, g);

        d.setContentPane(f); d.setVisible(true);
    }

    private void xoaPhuCap() {
        int row = tablePC.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chon khoan de xoa."); return; }

        int maPC = (int) modelPC.getValueAt(row, 0);
        String ten = (String) modelPC.getValueAt(row, 2);
        String tt = (String) modelPC.getValueAt(row, 6);

        if (tt.contains("Ngung")) {
            JOptionPane.showMessageDialog(this, "Khoan nay da ngung.");
            return;
        }

        int cf = JOptionPane.showConfirmDialog(this,
                "Ngung khoan '" + ten + "'?\n(Se khong ap dung khi tinh luong nua)",
                "Xac nhan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (cf == JOptionPane.YES_OPTION) {
            ServiceResult<Void> res = svc.xoaCauHinhPC(maPC);
            JOptionPane.showMessageDialog(this, res.getMessage());
            loadPhuCap();
        }
    }

    // ═══════════════════════════════════
    //  TAB — EMPLOYEE (TODO)
    // ═══════════════════════════════════
    private JPanel tabCaNhan() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(UIColors.WHITE);
        JLabel l = new JLabel("Chuc nang cham cong ca nhan — Dang phat trien...");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 16)); l.setForeground(UIColors.TEXT_GRAY);
        p.add(l); return p;
    }

    // ═══════════════════════════════════
    //  HELPER
    // ═══════════════════════════════════
    private String fmtTien(double a) { return moneyFmt.format(Math.round(a)) + " d"; }
    private JPanel panel() {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(UIColors.WHITE);
        p.setBorder(new EmptyBorder(15, 15, 15, 15)); return p;
    }
    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text); b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }
    private JTable tbl(DefaultTableModel m) {
        JTable t = new JTable(m); t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(Color.BLACK); t.setRowHeight(32);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(UIColors.PRIMARY_PURPLE);
        t.getTableHeader().setForeground(Color.BLACK);
        t.setSelectionBackground(UIColors.LIGHT_PURPLE);
        t.setSelectionForeground(Color.BLACK);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(table, v, s, f, r, c);
                setForeground(Color.BLACK); return this;
            }
        });
        return t;
    }
    private DefaultTableModel mdl(String[] cols) {
        return new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    }
    private JLabel lbl(String label, String val, Color color) {
        JLabel l = new JLabel(label + " " + val); l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(color); return l;
    }
    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8); g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL; return g;
    }
    private JComboBox<String> combMonth() {
        JComboBox<String> c = new JComboBox<>();
        for (int i = 1; i <= 12; i++) c.addItem(String.valueOf(i));
        c.setSelectedIndex(LocalDate.now().getMonthValue() - 1); return c;
    }
    private JComboBox<String> combYear() {
        JComboBox<String> c = new JComboBox<>(); int y = LocalDate.now().getYear();
        for (int yr = y - 2; yr <= y; yr++) c.addItem(String.valueOf(yr));
        c.setSelectedIndex(2); return c;
    }
    private static class StatusR extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            String str = v != null ? v.toString() : "";
            if (str.contains("muon") || str.contains("som") || str.contains("choi")) setForeground(UIColors.DANGER_RED);
            else if (str.contains("Cho") || str.contains("Chua")) setForeground(new Color(200, 150, 0));
            else if (str.contains("Dung") || str.contains("duyet") || str.contains("Hoat dong") || str.contains("tinh"))
                setForeground(UIColors.SUCCESS_GREEN);
            else if (str.contains("Vang") || str.contains("Ngung")) setForeground(Color.GRAY);
            else setForeground(UIColors.TEXT_DARK);
            setHorizontalAlignment(CENTER); return this;
        }
    }
}