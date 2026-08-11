package tw.com.insurance.api.inquiry.util;

import static tw.com.insurance.api.inquiry.dto.UnderwritingInquiryDtos.InquiryDetail;
import static tw.com.insurance.api.inquiry.dto.UnderwritingInquiryDtos.InquiryItem;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/** 將已整理完成的核保照會資料渲染成 PDF，不負責查詢或核保判斷。 */
public final class UnderwritingInquiryPdfGenerator {
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

	private UnderwritingInquiryPdfGenerator() {
	}

	/**
	 * 產生包含案件、關係人與未通過項目的核保照會單 PDF。
	 *
	 * @param detail
	 *            已完成個資遮蔽與繁中狀態轉換的照會資料
	 * @return 完整 PDF 位元組
	 */
	public static byte[] generate(InquiryDetail detail) {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			Document document = new Document(PageSize.A4, 42, 42, 48, 48);
			PdfWriter writer = PdfWriter.getInstance(document, output);
			byte[] fontBytes = loadFont();
			BaseFont base = BaseFont.createFont("NotoSansTC-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true,
					fontBytes, null);
			Font title = new Font(base, 20, Font.BOLD, new Color(15, 118, 110));
			Font heading = new Font(base, 12, Font.BOLD, Color.WHITE);
			Font body = new Font(base, 10, Font.NORMAL, new Color(31, 41, 51));
			Font muted = new Font(base, 9, Font.NORMAL, new Color(90, 103, 115));
			writer.setPageEvent(new FooterEvent(base));
			document.open();
			Paragraph p = new Paragraph("核保照會單", title);
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(6);
			document.add(p);
			Paragraph sub = new Paragraph("新契約核保結果暨補件通知", muted);
			sub.setAlignment(Element.ALIGN_CENTER);
			sub.setSpacingAfter(18);
			document.add(sub);
			addSection(document, "案件資料", heading);
			PdfPTable info = new PdfPTable(new float[]{1, 2, 1, 2});
			info.setWidthPercentage(100);
			info.setSpacingAfter(16);
			addPair(info, "照會單號", detail.inquiryNo(), body, base);
			addPair(info, "保單號碼", displayPolicyNo(detail.policyNo()), body, base);
			addPair(info, "要保書號碼", detail.applicationNo(), body, base);
			addPair(info, "核保案件號", detail.underwritingCaseNo(), body, base);
			addPair(info, "要保書版本", String.valueOf(detail.applicationRevision()), body, base);
			addPair(info, "核保結果", detail.underwritingStatusDescription(), body, base);
			addPair(info, "處理狀態", detail.inquiryStatusDescription(), body, base);
			addPair(info, "核保決定", detail.decisionDescription(), body, base);
			addPair(info, "新契約階段", detail.newContractStageDescription(), body, base);
			addPair(info, "契約狀態", detail.contractStatusDescription(), body, base);
			addPair(info, "照會日期", TIME.format(detail.issuedAt()), body, base);
			addPair(info, "號碼性質", "預編保單號碼", body, base);
			document.add(info);
			addSection(document, "契約關係人與投保資料", heading);
			PdfPTable parties = new PdfPTable(new float[]{1, 2, 1, 2});
			parties.setWidthPercentage(100);
			parties.setSpacingAfter(16);
			addPair(parties, "要保人", detail.applicantNameMasked() + "（" + detail.applicantCustomerReference() + "）",
					body, base);
			addPair(parties, "被保險人", detail.insuredNameMasked() + "（" + detail.insuredCustomerReference() + "）", body,
					base);
			addPair(parties, "商品代碼", detail.productCode(), body, base);
			addPair(parties, "要保日期", String.valueOf(detail.applicationDate()), body, base);
			addPair(parties, "預定生效日", String.valueOf(detail.requestedEffectiveDate()), body, base);
			addPair(parties, "保險金額", money(detail.currencyCode(), detail.sumAssuredAmount()), body, base);
			addPair(parties, "首期保險費", money(detail.currencyCode(), detail.premiumAmount()), body, base);
			addPair(parties, "資料揭露", "敏感個資不顯示", body, base);
			document.add(parties);
			addSection(document, "未通過檢核與應補事項", heading);
			PdfPTable items = new PdfPTable(new float[]{.7f, 1.8f, 4.5f});
			items.setWidthPercentage(100);
			items.setHeaderRows(1);
			items.setSpacingAfter(16);
			addHeader(items, "項次", heading);
			addHeader(items, "檢核項目", heading);
			addHeader(items, "照會內容", heading);
			int index = 1;
			for (InquiryItem item : detail.items()) {
				addBody(items, String.valueOf(index++), body);
				addBody(items, item.ruleName(), body);
				addBody(items, item.itemMessage(), body);
			}
			document.add(items);
			addSection(document, "回覆與送件說明", heading);
			document.add(new Paragraph("請依各項照會內容補齊資料，並由承辦人員確認文件清晰、簽署完整及資料一致後，再送回核保審查。本照會單表示案件目前尚未通過核保，並非保險契約已成立或已拒絕承保。",
					body));
			Paragraph note = new Paragraph("本文件由系統依核保照會紀錄產生，請以系統最新案件狀態為準。", muted);
			note.setSpacingBefore(18);
			document.add(note);
			document.close();
			return output.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("核保照會單 PDF 產生失敗", e);
		}
	}
	/** 加入具有一致色彩與間距的段落標題。 */
	private static void addSection(Document document, String text, Font font) throws DocumentException {
		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(100);
		table.setSpacingBefore(4);
		table.setSpacingAfter(8);
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setBackgroundColor(new Color(15, 118, 110));
		cell.setPadding(7);
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		document.add(table);
	}
	/** 加入一組標籤與內容欄位，空值統一顯示連字號。 */
	private static void addPair(PdfPTable table, String label, String value, Font body, BaseFont base) {
		Font labelFont = new Font(base, 9, Font.BOLD, new Color(71, 85, 105));
		PdfPCell l = new PdfPCell(new Phrase(label, labelFont));
		l.setBackgroundColor(new Color(241, 245, 249));
		l.setPadding(7);
		l.setBorderColor(new Color(203, 213, 225));
		table.addCell(l);
		addBody(table, value == null ? "-" : value, body);
	}
	/** 加入照會項目表格的欄位標題。 */
	private static void addHeader(PdfPTable table, String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setBackgroundColor(new Color(51, 65, 85));
		cell.setPadding(7);
		table.addCell(cell);
	}
	/** 加入照會項目表格的一般內容。 */
	private static void addBody(PdfPTable table, String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setPadding(7);
		cell.setBorderColor(new Color(203, 213, 225));
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		table.addCell(cell);
	}
	/** 從 classpath 載入可嵌入 PDF 的繁體中文字型。 */
	private static byte[] loadFont() {
		try (var stream = UnderwritingInquiryPdfGenerator.class.getClassLoader()
				.getResourceAsStream("fonts/ttf/NotoSansTC/NotoSansTC-Regular.ttf")) {
			if (stream == null)
				throw new IllegalStateException("找不到 Noto Sans TC 字型資源");
			return stream.readAllBytes();
		} catch (Exception e) {
			throw new IllegalStateException("載入 PDF 繁體中文字型失敗", e);
		}
	}
	/** 將尚未取號的保單號碼轉為使用者可理解的文字。 */
	private static String displayPolicyNo(String policyNo) {
		return policyNo == null || policyNo.isBlank() ? "尚未編發" : policyNo;
	}
	/** 以幣別與不含多餘尾零的金額產生 PDF 顯示文字。 */
	private static String money(String currency, java.math.BigDecimal amount) {
		return currency + " " + amount.stripTrailingZeros().toPlainString();
	}
	/** 在每一頁結束時繪製一致的繁中頁碼。 */
	private static class FooterEvent extends PdfPageEventHelper {
		private final BaseFont base;
		FooterEvent(BaseFont base) {
			this.base = base;
		}
		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			PdfContentByte canvas = writer.getDirectContent();
			canvas.beginText();
			canvas.setFontAndSize(base, 8);
			canvas.setColorFill(new Color(100, 116, 139));
			canvas.showTextAligned(Element.ALIGN_CENTER, "第 " + writer.getPageNumber() + " 頁",
					PageSize.A4.getWidth() / 2, 24, 0);
			canvas.endText();
		}
	}
}
