package simbryo.synthoscopy.microscope.lightsheet.gui;

import java.util.Arrays;
import java.util.Collection;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulator;
import simbryo.synthoscopy.microscope.parameters.CameraParameter;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.ParameterInterface;
import simbryo.synthoscopy.microscope.parameters.StageParameter;

/**
 * JavaFX Panel for controling the parameters of a lightsheet micorscope
 * simulator
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulatorPanel extends TabPane
{

  /**
   * Creates a panel for controling the parameters of a lightsheet micorscope
   * 
   * @param pSimulator
   *          simulator
   */
  public LightSheetMicroscopeSimulatorPanel(LightSheetMicroscopeSimulator pSimulator)
  {
    super();

    setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

    int lNumberOfLightSheets = pSimulator.getNumberOfLightSheets();
    int lNumberOfDetectionPaths =
                                pSimulator.getNumberOfDetectionArms();

    {
      Tab tab = new Tab();
      tab.setText("Stage");
      tab.setContent(setupParameterControls(pSimulator,
                                            StageParameter.values(),
                                            true,
                                            0));
      getTabs().add(tab);
    }

    for (int i = 0; i < lNumberOfLightSheets; i++)
    {
      Tab tab = new Tab();
      tab.setText("Illumination" + i);
      tab.setContent(setupParameterControls(pSimulator,
                                            IlluminationParameter.values(),
                                            true,
                                            i));
      getTabs().add(tab);
    }

    for (int i = 0; i < lNumberOfDetectionPaths; i++)
    {
      Tab tab = new Tab();
      tab.setText("Detection" + i);
      tab.setContent(setupParameterControls(pSimulator,
                                            DetectionParameter.values(),
                                            true,
                                            i));
      getTabs().add(tab);
    }

    for (int i = 0; i < lNumberOfDetectionPaths; i++)
    {
      Tab tab = new Tab();
      tab.setText("Camera" + i);
      tab.setContent(setupParameterControls(pSimulator,
                                            CameraParameter.values(),
                                            true,
                                            i));
      getTabs().add(tab);
    }

  }

  private Node setupParameterControls(LightSheetMicroscopeSimulator pSimulator,
                                      ParameterInterface<Number>[] pParameterArray,
                                      boolean sendEventsWhileDragging,
                                      int pIndex)
  {
    return setupParameterControls(pSimulator,
                                  Arrays.asList(pParameterArray),
                                  sendEventsWhileDragging,
                                  pIndex);
  }

  private Node setupParameterControls(LightSheetMicroscopeSimulator pSimulator,
                                      Collection<ParameterInterface<Number>> pParameterList,
                                      boolean sendEventsWhileDragging,
                                      int pIndex)
  {
    GridPane lGridPane = new GridPane();

    lGridPane.setHgap(5);
    lGridPane.setVgap(5);
    lGridPane.setPadding(new Insets(5, 5, 5, 5));

    ColumnConstraints col1 = new ColumnConstraints();
    col1.setMinWidth(5);

    ColumnConstraints col2 = new ColumnConstraints();
    col2.setFillWidth(true);
    col2.setHgrow(Priority.ALWAYS);

    lGridPane.getColumnConstraints().addAll(col1, col2);/**/

    int i = 0;
    for (ParameterInterface<Number> lParameter : pParameterList)
    {
      addSlider(pSimulator,
                lParameter,
                pIndex,
                lGridPane,
                sendEventsWhileDragging,
                i++);
    }
    return lGridPane;
  }

  private void addSlider(LightSheetMicroscopeSimulator pSimulator,
                         ParameterInterface<Number> pParameter,
                         int pIndex,
                         GridPane pGridPane,
                         boolean sendEventsWhileDragging,
                         int pRowIndex)
  {
    double lDefaultValue = pParameter.getDefaultValue().doubleValue();
    double lMinValue = pParameter.getMinValue().doubleValue();
    double lMaxValue = pParameter.getMaxValue().doubleValue();

    Label lLabel = new Label(pParameter.toString());
    pGridPane.add(lLabel, 0, pRowIndex);

    Slider lSlider = new Slider(lMinValue, lMaxValue, lDefaultValue);

    lSlider.setShowTickLabels(true);
    lSlider.setShowTickMarks(false);
    lSlider.setBlockIncrement((lMaxValue - lMinValue) / 1000);
    lSlider.setMajorTickUnit((lMaxValue - lMinValue) / 10);
    lSlider.setMinorTickCount(10);
    // lSlider.setSnapToTicks(true);
    lSlider.setMaxWidth(Double.MAX_VALUE);
    lSlider.setOrientation(Orientation.HORIZONTAL);

    GridPane.setHgrow(lSlider, Priority.ALWAYS);

    if (sendEventsWhileDragging)
      lSlider.valueProperty().addListener((s, o, n) -> {
        if (n != o)
          pSimulator.setNumberParameter(pParameter,
                                        pIndex,
                                        n.doubleValue());
      });
    else
      lSlider.valueChangingProperty().addListener((s, o, n) -> {
        if (!n)
          pSimulator.setNumberParameter(pParameter,
                                        pIndex,
                                        lSlider.getValue());
      });

    GridPane.setVgrow(lSlider, Priority.ALWAYS);
    pGridPane.add(lSlider, 1, pRowIndex);
  }

}
