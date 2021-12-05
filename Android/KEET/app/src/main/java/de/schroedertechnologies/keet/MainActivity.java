package de.schroedertechnologies.keet;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SensorReaderCallback {

    // Configuration values
    private final int m_diagramHistoryLength = 64;

    // Plot related values
    private XYPlot m_rSensePlot;
    private SimpleXYSeries m_rSenseSeries;
    private Redrawer m_redrawer;
    private LineAndPointFormatter m_lineAndPointFormatter;

    private SensorReader m_sensorReader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_rSensePlot = findViewById(R.id.rSensePlot);
        m_rSenseSeries = new SimpleXYSeries(getString(R.string.ki_value));
        m_rSenseSeries.useImplicitXVals();
        m_rSensePlot.setRangeBoundaries(0, Math.pow(2, 16), BoundaryMode.AUTO);
        m_rSensePlot.setDomainBoundaries(0, m_diagramHistoryLength, BoundaryMode.FIXED);
        m_lineAndPointFormatter = new LineAndPointFormatter(Color.rgb(100, 100, 200), Color.rgb(0, 100, 0), null, null);
        m_lineAndPointFormatter.setInterpolationParams(new CatmullRomInterpolator.Params(20, CatmullRomInterpolator.Type.Centripetal));
        m_rSensePlot.addSeries(m_rSenseSeries, m_lineAndPointFormatter);
        m_rSensePlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        m_rSensePlot.setDomainStepValue(m_diagramHistoryLength / 4.0);
        m_rSensePlot.setLinesPerRangeLabel(3);
        m_rSensePlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("#"));

        m_rSensePlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("#"));

        m_redrawer = new Redrawer(
                Arrays.asList(new Plot[]{m_rSensePlot}),
                100, false);

        m_sensorReader = new SensorReader(this, this);

    }

    @Override
    protected void onDestroy() {
        m_sensorReader.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        m_sensorReader.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_sensorReader.connect();
    }

    @Override
    public void OnDeviceConnected() {
        // Start diagram
        m_redrawer.start();
    }

    @Override
    public void OnDeviceDisconnected() {
        // Stop diagram
        m_redrawer.pause();
    }

    @Override
    public void OnDeviceDataReceived(int sensVal, int avgSensVal, int bits) {

        if(m_rSenseSeries.size() > m_diagramHistoryLength) {
            m_rSenseSeries.removeFirst();
        }

        m_rSenseSeries.addLast(null, sensVal);

    }
}