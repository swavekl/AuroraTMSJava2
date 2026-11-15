/**
 * Summary report item consisting of two columns (item, price)
 * Sections of report will be
 */
export class SummaryReportItem {
  // to start new section and show text in bold
  isHeader: boolean;

  // left column
  itemText: string;

  // optional left column second item
  subItemText: string;

  // Optional third line used for doubles partner details.
  partnerText?: string;

  // right column text or
  rightColumnText: string;
}
