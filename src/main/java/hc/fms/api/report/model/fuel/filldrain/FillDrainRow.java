package hc.fms.api.report.model.fuel.filldrain;

import hc.fms.api.report.model.fuel.ColVal;
import hc.fms.api.report.model.fuel.StringVal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FillDrainRow {
	private ColVal<Long>  date;
	private ColVal<Double> volume;
	private ColVal<Double> max;
	private ColVal<Double> avg;
	private ColVal<Double> time;
	private ColVal<Double> value;
	private StringVal address;
}