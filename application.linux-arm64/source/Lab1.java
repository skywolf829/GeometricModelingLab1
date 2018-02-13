import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Lab1 extends PApplet {

final int numApproxPoints = 100;

String[] CurveOptions = {"Bezier Curve", 
  "B-Spline", 
  "Subdivision Curve", 
  "Subdivision Quadratic B-Spline"
};

Curve curve;
double[] currentCurveArgs;

Rectangle drawingArea;

RadioButtons curveSelector;
RadioButtons pointManipulation;
RadioButtons addPoint;

IntSlider numSubdivisions;
IntSlider BSplineDegree;
IntSlider Duplicity;
ArrayList<MoveablePoint> points;

int screenWidth = 700;
int screenHeight = 400;
int lastPointSelected = 0;


public void start() {
  // Add the drawing area
  drawingArea = new Rectangle(5, 5, screenWidth - 250 - 10, screenHeight - 10);
  drawingArea.c = color(255);

  // Add 2 random points to the drawing area
  points = new ArrayList<MoveablePoint>();
  for (int i = 0; i < 2; i++) {
    points.add(new MoveablePoint((int)random(drawingArea.width), (int)random(drawingArea.height), drawingArea));
  }
  // Add the radio buttons
  curveSelector = new RadioButtons(CurveOptions, drawingArea.width + 20, 10, 15, 15);
  pointManipulation = new RadioButtons(new String[] {"Add point", "Delete point", "Select point"}, 
    drawingArea.width + 20, 250, 10, 10);
  addPoint = new RadioButtons(new String[] {"Add point before selected", "Add point after selected"}, 
    drawingArea.width + 20, 320, 10, 10);
    
  // Add the sliders
  numSubdivisions = new IntSlider("Number of Subdivisions", 1, 7, drawingArea.width + 35, curveSelector.getHeight() + 30, 200);
  BSplineDegree = new IntSlider("Degree for BSpline", 2, 2, drawingArea.width + 35, curveSelector.getHeight() + 30, 200);
  Duplicity = new IntSlider("Duplicity of points", 1, 3, drawingArea.width + 35, curveSelector.getHeight() + 80, 200);

  // Create a Bezier curve
  curve = new BezierCurve();
  curve.controlPoints = ToPoints(points);
  currentCurveArgs = new double[]{numApproxPoints, 0};
  curve.approximateCurve(currentCurveArgs);
}

public void setup() {
  
  
  background(0);
}

public void mousePressed() {
  // Save this in case we switch
  int lastCurveSelected = curveSelector.selectedIndex;

  curveSelector.mousePressed();

  pointManipulation.mousePressed();
  // These buttons can only be pressed if adding a point is selected
  if (pointManipulation.selectedIndex == 0) {
    addPoint.mousePressed();
  }

  CheckPointsClicked();
  CheckAddPoint();

  // update duplicity
  if (curveSelector.selectedIndex == 0 ||
    curveSelector.selectedIndex == 1) {
    Duplicity.mousePressed();
    currentCurveArgs[1] = Duplicity.getValue();
  }
  // update BSpline degree
  if (curveSelector.selectedIndex == 1) {
    BSplineDegree.mousePressed();
    currentCurveArgs[0] = BSplineDegree.getValue();
  }
  // update subdivisions
  if (curveSelector.selectedIndex == 2 ||
    curveSelector.selectedIndex == 3) {
    numSubdivisions.mousePressed();
  }

  // if the curve was changed
  if (lastCurveSelected != curveSelector.selectedIndex) {
    if (curveSelector.selectedIndex == 0) {
      curve = new BezierCurve();
      currentCurveArgs = new double[]{numApproxPoints, Duplicity.getValue()};
    } else if (curveSelector.selectedIndex == 1) {
      curve = new BSpline();
      currentCurveArgs = new double[]{BSplineDegree.getValue(), Duplicity.getValue()};
    } else if (curveSelector.selectedIndex == 2) {
      curve = new SubdivisionCurve();
      currentCurveArgs = new double[]{numSubdivisions.getValue(), 0};
    } else if (curveSelector.selectedIndex == 3) {
      curve = new SubdivisionQuadraticBSpline();
      currentCurveArgs = new double[]{numSubdivisions.getValue(), 0};
    }
    curve.controlPoints = ToPoints(points);
    curve.approximateCurve(currentCurveArgs);
  }
}
public void mouseDragged() {
  boolean changed = false;
  // Update duplicity
  if (curveSelector.selectedIndex == 0
    || curveSelector.selectedIndex == 1) {
    int oldDuplicity = Duplicity.getValue();
    Duplicity.mouseDragged();
    if (oldDuplicity != Duplicity.getValue()) {
      currentCurveArgs[1] = Duplicity.getValue();
      changed = true;
    }
  }
  if (curveSelector.selectedIndex == 1) {
    int oldDegree = BSplineDegree.getValue();
    BSplineDegree.mouseDragged();
    if (BSplineDegree.getValue() != oldDegree) {
      currentCurveArgs[0] = BSplineDegree.getValue();
      changed = true;
    }
  }

  if (curveSelector.selectedIndex == 2 || curveSelector.selectedIndex == 3) {
    int oldSubdivisions = numSubdivisions.getValue();
    numSubdivisions.mouseDragged();
    if (numSubdivisions.getValue() != oldSubdivisions) {
      currentCurveArgs[0] = numSubdivisions.getValue();
      changed = true;
    }
  }
  for (int i = 0; i < points.size(); i++) {
    double oldx = points.get(i).x;
    double oldy = points.get(i).y;
    points.get(i).mouseDragged();
    if (oldx != points.get(i).x || oldy != points.get(i).y) changed = true;
  }
  curve.controlPoints = ToPoints(points);
  if (changed)
    curve.approximateCurve(currentCurveArgs);
}

public void mouseReleased() {
  if (curveSelector.selectedIndex == 0) {
    Duplicity.mouseReleased();
  }
  if (curveSelector.selectedIndex == 1) {
    BSplineDegree.mouseReleased();
    Duplicity.mouseReleased();
  }
  if (curveSelector.selectedIndex == 2 || curveSelector.selectedIndex == 3) {
    numSubdivisions.mouseReleased();
  }
  for (int i = 0; i < points.size(); i++) {
    points.get(i).mouseReleased();
  }
}

public void draw() {
  background(0);
  stroke(255);

  drawingArea.draw();

  curveSelector.draw();
  textSize(12);

  for (int i = 0; i < points.size(); i++) {
    if (lastPointSelected == i) {
      noStroke();
      fill(0, 255, 0);
      ellipse(points.get(i).circle.x, points.get(i).circle.y, 
        points.get(i).circle.width + 4, points.get(i).circle.height + 4);
    }
    noStroke();
    points.get(i).draw();    
    stroke(0, 0, 255);
    fill(0, 0, 255);
    text(i+1, (int)points.get(i).x, (int)points.get(i).y + 20);
  }
  if (curveSelector.selectedIndex == 0) {
    fill(255);
    text("Duplicity of points", drawingArea.width + 35, curveSelector.getHeight() + 70);
    Duplicity.draw();
  }
  if (curveSelector.selectedIndex == 1) {
    fill(255);
    text("Degree of BSpline", drawingArea.width + 35, curveSelector.getHeight()+ 20);
    BSplineDegree.draw();
    text("Duplicity of points", drawingArea.width + 35, curveSelector.getHeight() + 70);
    Duplicity.draw();
  }
  if (curveSelector.selectedIndex == 2 || curveSelector.selectedIndex == 3) {
    fill(255);
    text("Number of Subdivisions", drawingArea.width + 35, curveSelector.getHeight()+ 20);
    numSubdivisions.draw();
  }
  curve.draw();
  pointManipulation.draw();
  if (pointManipulation.selectedIndex == 0)
    addPoint.draw();
}



public void CheckPointsClicked() {
  for (int i = 0; i < points.size(); i++) {
    // See if they select a point and hold id
    if (pointManipulation.selectedIndex == 2) {
      points.get(i).mousePressed();   
      if (points.get(i).holding) lastPointSelected = i;
    }
    if (points.get(i).holding) return;
    // See if they delete a point
    if (pointManipulation.selectedIndex == 1 && points.get(i).circle.contains(mouseX, mouseY)) {
      points.remove(i);
      BSplineDegree.setMax(points.size());
      if (curveSelector.selectedIndex == 1) {
        currentCurveArgs[0] = BSplineDegree.getValue();
      }
      curve.controlPoints = ToPoints(points);
      curve.approximateCurve(currentCurveArgs);
      return;
    }
  }
}

public void CheckAddPoint() {
  // See if we add a point
  if (pointManipulation.selectedIndex == 0 && drawingArea.contains(mouseX, mouseY)) {
    int index = 0;
    if (addPoint.selectedIndex == 0) index = lastPointSelected;
    else index = lastPointSelected+1;
    points.add(index, new MoveablePoint(mouseX, mouseY, drawingArea));
    ArrayList<Point> p = new ArrayList<Point>();
    for (int i = 0; i < points.size(); i++) {
      p.add(points.get(i).toPoint());
    }
    BSplineDegree.setMax(points.size());
    if (curveSelector.selectedIndex == 1) {
      currentCurveArgs[0] = BSplineDegree.getValue();
    }
    curve.controlPoints = p;
    curve.approximateCurve(currentCurveArgs);
  }
}
class RadioButton {
  public boolean selected = false;
  public Circle c;
  public String name;
  public RadioButton(String n, int x, int y, int width, int height) {
    c = new Circle(x, y, width, height);
    c.c = color(255);
    this.name = n;
  }

  public void draw() {
    c.draw();  
    textSize(c.height);
    text(name, c.x + c.width, c.y + c.height / 2.0f);  
    if (selected) {
      fill(0);
      ellipse(c.x, c.y, c.width - 2, c.height - 2);
    }
  }
}

class RectangleButton {
  public Rectangle r;
  public String name;
  public RectangleButton(String n, int x, int y, int width, int height) {
    r = new Rectangle(x, y, width, height);
    this.name = n;
    r.c = color(200);
  }
  public void draw() {
    r.draw();
    fill(255);
    textSize(r.height - 4);
    text(name, r.x, r.y + r.height / 2.0f + 10);
  }
}

class RadioButtons {
  ArrayList<RadioButton> buttons;
  public String[] options;
  public int x, y, height, spacing;
  public int selectedIndex = 0;
  public RadioButtons(String[] options, int x, int y, int height, int spacing) {
    buttons = new ArrayList<RadioButton>();
    this.options = options;
    this.x = x;
    this.y = y;
    this.height = height;
    this.spacing = spacing;

    for (int i = 0; i < options.length; i++) {
      buttons.add(new RadioButton(options[i], x, y + i * (height + spacing), height, height));
    }
    buttons.get(0).selected = true;
  }
  public int getHeight() {
    return buttons.size() * (height + spacing);
  }
  public void mousePressed() {
    boolean clickHandled = false;
    int clickedButton = -1;
    for (int i = 0; i < buttons.size(); i++) {
      if (buttons.get(i).c.contains(mouseX, mouseY)) {
        buttons.get(i).selected = true;
        clickHandled = true;
        selectedIndex = i;
        clickedButton = i;
      }
    }
    if (clickHandled) {
      for (int i = 0; i < buttons.size(); i++) {
        if (clickedButton != i) {
          buttons.get(i).selected = false;
        }
      }
    }
  }

  public void draw() {
    for (int i = 0; i < buttons.size(); i++) {
      buttons.get(i).draw();
    }
  }
}
abstract class Curve {
  public ArrayList<Point> controlPoints;
  public ArrayList<Point> approximatePoints;
  public abstract void approximateCurve(double[] args);
  public void draw() {
    stroke(0);
    for (int i = 0; i < approximatePoints.size() - 1; i++) {
      //ellipse((int)approximatePoints.get(i).x, (int)approximatePoints.get(i).y, 2, 2);
      line((int)approximatePoints.get(i).x, (int)approximatePoints.get(i).y, 
        (int)approximatePoints.get(i+1).x, (int)approximatePoints.get(i+1).y);
    }
    stroke(255);
  }
}


//Bezier curve - one curve for all the points
class BezierCurve extends Curve {

  public BezierCurve() {
    controlPoints = new ArrayList<Point>();
    approximatePoints = new ArrayList<Point>();
  }
  public Point SolveAtParameterBernstein(float u) {
    Point p = new Point(0, 0);
    for (int i = 0; i < controlPoints.size(); i++) {
      p.x += controlPoints.get(i).x * nChoosek(controlPoints.size() - 1, i) * 
        pow(u, i) * pow(1 - u, controlPoints.size() - i - 1);
      p.y += controlPoints.get(i).y * nChoosek(controlPoints.size() - 1, i) * 
        pow(u, i) * pow(1 - u, controlPoints.size() - i - 1);
    }
    p.circle.x = (int)p.x;
    p.circle.y = (int)p.y;
    return p;
  }

  public void approximateCurve(double[] args) {
    ArrayList<Point> cpsave = controlPoints;
    int n = cpsave.size();
    for (int j = 0; j < n; j++) {
      for (int i = 1; i < args[1]; i++) {
        controlPoints.add(j * (int)args[1], controlPoints.get(j * (int)args[1]));
      }
    }
    approximatePoints = new ArrayList<Point>();
    for (float i = 0.0f; i <= 1; i += 1.0f / args[0]) {
      approximatePoints.add(SolveAtParameterBernstein(i));
    }
    controlPoints = cpsave;
  }
}

//Cubic B-spline with uniform knot vector.
class BSpline extends Curve {
  int D;
  public BSpline() {
    controlPoints = new ArrayList<Point>();
    approximatePoints = new ArrayList<Point>();
  }
  public void approximateCurve(double[] args) {
    ArrayList<Point> cpsave = controlPoints;
    int n = cpsave.size();
    for (int j = 0; j < n; j++) {
      for (int i = 1; i < args[1]; i++) {
        controlPoints.add(j * (int)args[1], controlPoints.get(j * (int)args[1]));
      }
    }
    D = (int)args[0];
    approximatePoints = new ArrayList<Point>();

    for (float i = 0.0f; i <= controlPoints.size() - D + 1; i+=.01f) {
      approximatePoints.add(SolveAt(i, D));
    }
    controlPoints = cpsave;
  }
  public Point SolveAt(double u, int D) {
    Point p = new Point(0, 0);


    for (int i = 0; i < controlPoints.size(); i++) {
      p.x += controlPoints.get(i).x * basisFunction(i, D, u);
      p.y += controlPoints.get(i).y * basisFunction(i, D, u);
    }

    return p;
  }
  public float getT(int j) {

    if (D <= j && j <= controlPoints.size()-1) {
      return j-D+1;
    }
    if (controlPoints.size() - 1 < j && j <= controlPoints.size()-1+D) {
      return controlPoints.size()-D+1;
    } else {
      return 0;
    }
  }
  public double uniformBasisFunction(int i, int d, double u) {
    if (d == 1) {
      if (i <= u && 
        u < i+1) return 1;
      else return 0;
    } else {
      double leftSide, rightSide;

      leftSide = ((u-i) * uniformBasisFunction(i, d-1, u)) /
        ((float)(d-1));
      rightSide = ((i+d-u) * uniformBasisFunction(i+1, d-1, u)) /
        ((float)(d-1));

      return leftSide + rightSide;
    }
  }
  public double basisFunction(int i, int d, double u) {
    if (d == 1) {
      if (getT(i) <= u && 
        u < getT(i+1)) return 1;
      else return 0;
    } else {
      double leftSide, rightSide;
      if (getT(i+d-1) == 
        getT(i)) {
        leftSide = 0;
      } else {
        leftSide = ((u - getT(i)) * basisFunction(i, d-1, u)) /
          (getT(i+d-1) - getT(i));
      }
      if (getT(i+d) == 
        getT(i+1)) {
        rightSide = 0;
      } else {
        rightSide = ((getT(i+d) - u) * basisFunction(i+1, d-1, u) /
          (getT(i+d) - getT(i+1)));
      }
      return leftSide + rightSide;
    }
  }
}

//Subdivision curves using repeated de Casteljau method.
class SubdivisionCurve extends Curve {
  public SubdivisionCurve() {
    controlPoints = new ArrayList<Point>();
    approximatePoints = new ArrayList<Point>();
  }
  public void approximateCurve(double[] args) {
    approximatePoints = subdivide(controlPoints, (int)args[0], 0.5f);
  }
  private ArrayList<Point> oneSubdivide(ArrayList<Point> points, 
    ArrayList<Point> poly1, ArrayList<Point> poly2, double u) {
    if (points.size() == 1) {
      ArrayList<Point> toReturn = new ArrayList<Point>();
      for (int i = 0; i < poly1.size(); i++) {
        toReturn.add(poly1.get(i));
      }
      toReturn.add(points.get(0));
      for (int i = 0; i < poly2.size(); i++) {
        toReturn.add(poly2.get(i));
      }
      return toReturn;
    } else {
      poly1.add(points.get(0));
      poly2.add(0, points.get(points.size() - 1));
      ArrayList<Point> newPoints = new ArrayList<Point>();
      for (int i = 0; i < points.size() - 1; i++) {
        newPoints.add(new Point(
          points.get(i).x + u * (points.get(i+1).x - points.get(i).x), 
          points.get(i).y + u * (points.get(i+1).y - points.get(i).y))
          );
      }
      return oneSubdivide(newPoints, poly1, poly2, u);
    }
  }
  public ArrayList<Point> subdivide(ArrayList<Point> points, int m, double u) {
    if (m == 1) return oneSubdivide(points, new ArrayList<Point>(), new ArrayList<Point>(), u); 
    else {
      ArrayList<Point> newPoints = oneSubdivide(points, new ArrayList<Point>(), new ArrayList<Point>(), u); 
      ArrayList<Point> p1Points = new ArrayList<Point>();
      ArrayList<Point> p2Points = new ArrayList<Point>();

      for (int i = 0; i <= (newPoints.size() - 1) / 2; i++) {
        p1Points.add(newPoints.get(i));
      }
      for (int i = (newPoints.size() - 1) / 2; i < newPoints.size(); i++) {
        p2Points.add(newPoints.get(i));
      }


      ArrayList<Point> p1 = subdivide(p1Points, m-1, u);
      ArrayList<Point> p2 = subdivide(p2Points, m-1, u);
      ArrayList<Point> toReturn = new ArrayList<Point>();
      for (int i = 0; i < p1.size(); i++) {
        toReturn.add(p1.get(i));
      }
      for (int i = 0; i < p2.size(); i++) {
        toReturn.add(p2.get(i));
      }
      return toReturn;
    }
  }
}

//Subdivision Quadric B-spline with uniform knot vector.
class SubdivisionQuadraticBSpline extends Curve {
  public SubdivisionQuadraticBSpline() {
  }
  public void approximateCurve(double[] args) {
    approximatePoints = new ArrayList<Point>();
    approximatePoints = controlPoints;
    for (int i = 0; i < args[0]; i ++) {
      approximatePoints = subdivide(approximatePoints);
    }
  }
  public ArrayList<Point> subdivide(ArrayList<Point> points) {
    ArrayList<Point> newPoints = new ArrayList<Point>();
    for (int i = 1; i < points.size() - 1; i++) {
      Point q1 = new Point(0, 0);
      Point q2 = new Point(0, 0);
      Point r1 = new Point(0, 0);
      Point r2 = new Point(0, 0);
      if (i == 1) {
        q1.x = (3 / 4.0f) * points.get(i-1).x + (1 / 4.0f) * points.get(i).x;
        q1.y = (3 / 4.0f) * points.get(i-1).y + (1 / 4.0f) * points.get(i).y;

        q2.x = (1 / 4.0f) * points.get(i-1).x + (3 / 4.0f) * points.get(i).x;
        q2.y = (1 / 4.0f) * points.get(i-1).y + (3 / 4.0f) * points.get(i).y;
      }
      r1.x = (3 / 4.0f) * points.get(i).x + (1 / 4.0f) * points.get(i+1).x;
      r1.y = (3 / 4.0f) * points.get(i).y + (1 / 4.0f) * points.get(i+1).y;

      r2.x = (1 / 4.0f) * points.get(i).x + (3 / 4.0f) * points.get(i+1).x;
      r2.y = (1 / 4.0f) * points.get(i).y + (3 / 4.0f) * points.get(i+1).y;

      if (i == 1) {
        newPoints.add(q1);
        newPoints.add(q2);
      }
      newPoints.add(r1);
      newPoints.add(r2);
    }
    return newPoints;
  }
}


class IntSlider {
  int min, max, x, y, size;
  String name;
  Rectangle background;
  Rectangle slider; 
  boolean holding = false;
  int sliderSize;
  public IntSlider(String name, int min, int max, int x, int y, int size) {
    this.min = min;
    this.max = max;
    this.name = name;
    this.x = x;
    this.y = y; 
    this.size = size;
    sliderSize = size / 15;
    background = new Rectangle(x, y, size, sliderSize);
    slider = new Rectangle(x, y - 5, sliderSize / 2, sliderSize + 10);
    slider.c = color(0);
    background.c = color(0);
  }
  public void setMin(int n){
    min = n;
  }
  public void setMax(int n){
    max = n;
  }
  public int getValue() {
    int closest = 0;
    float dist = distance(x, y, slider.x, slider.y);
    for (int i = 1; i <= max - min; i += 1) {
      float d = distance(slider.x, slider.y, x + i * (size / (max - min)), y);
      if (dist > d) {
        dist =  d;
        closest = i;
      }
    }
    return min + closest;
  }
  public void draw() {

    background.draw();
    slider.draw();
    fill(255);
    textSize(size / 20);
    text(min, x - sliderSize * (min + "").length() - 5, y + sliderSize);
    text(max, x + size + 5, y + sliderSize);
    text(getValue(), slider.x, slider.y + slider.height + sliderSize);
  }

  public void mousePressed() {
    if (slider.contains(mouseX, mouseY)) {
      holding = true;
    }
  }
  public void mouseDragged() {
    if (holding) {
      slider.x = mouseX;
      if (slider.x < x) slider.x = x;
      if (slider.x > x + size) slider.x = x + size;
    }
  }
  public void mouseReleased() {
    holding = false;
  }
}

class Point {
  public double x, y;
  public Circle circle;
  public int c;
  public Point(double x, double y) {
    this.x = x; 
    this.y = y;
    circle = new Circle((int)x, (int)y, 3, 3);
    circle.c = color(0);
  }
  public void draw() {
    circle.draw();
  }
}
class MoveablePoint extends Point {
  public Circle circle;
  public boolean holding = false;
  public Rectangle bounds;
  public MoveablePoint(int x, int y, Rectangle bounds) {
    super(x, y);
    this.x = x;
    this.y = y;
    circle = new Circle(x, y, 20, 20);
    circle.c = color(0, 0, 255);
    this.bounds = bounds;
  }
  public Point toPoint() {
    return new Point(x, y);
  }
  public void draw() {
    circle.c = color(0, 0, 255);
    if (holding) circle.c = color(255, 0, 0);
    circle.draw();
  }
  public void mousePressed() {
    if (circle.contains(mouseX, mouseY)) {
      holding = true;
    }
  }
  public void mouseDragged() {
    if (holding) {
      x = mouseX;
      y = mouseY;
      circle.x = mouseX;
      circle.y = mouseY;
      if (circle.x < bounds.x) {
        circle.x = bounds.x;
        x = bounds.x;
      }
      if (circle.y < bounds.y) {
        circle.y = bounds.y;
        y = bounds.y;
      }
      if (circle.x > bounds.x + bounds.width) {
        circle.x = bounds.x + bounds.width;
        x = bounds.x + bounds.width;
      }
      if (circle.y > bounds.y + bounds.height) {
        circle.y = bounds.y + bounds.height;
        y = bounds.y + bounds.height;
      }
    }
  }
  public void mouseReleased() {
    holding = false;
  }
}

class Rectangle {
  public int x, y, width, height;
  public int c;
  public Rectangle(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
  public void draw() {
    fill(c);
    rect(x, y, width, height);
  }
  public boolean contains(float x, float y) {
    return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
  }
}

class Circle {
  public int x, y, width, height;
  public int c;
  public Circle(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
  public boolean contains(float x, float y) {
    return x >= this.x - width / 2.0f && x <= this.x + width / 2.0f && 
      y >= this.y - height / 2.0f && y <= this.y + height / 2.0f;
  }
  public void draw() {
    fill(c);
    ellipse(x, y, width, height);
  }
}
public static double distance(Point p1, Point p2) {
  return Math.pow(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2), 0.5f);
}
public static float distance(float x1, float y1, float x2, float y2) {
  return pow(pow(x1 - x2, 2) + pow(y1 - y2, 2), 0.5f);
}
public static double nChoosek(int n, int k) {
  return fact(n) / (fact(k) * fact(n - k));
}
public static final double fact(int num) {
  double i = 1;
  while (num > 0) {
    i *= num;
    num--;
  }
  return i;
}
public static ArrayList<Point> ToPoints(ArrayList<MoveablePoint> points) {
  ArrayList<Point> p = new ArrayList<Point>();
  for (int i = 0; i < points.size(); i++) {
    p.add(points.get(i).toPoint());
  }
  return p;
}
  public void settings() {  size(700, 400);  noSmooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Lab1" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
