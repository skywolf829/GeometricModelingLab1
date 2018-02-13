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


void start() {
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

void setup() {
  size(700, 400);
  noSmooth();
  background(0);
}

void mousePressed() {
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
void mouseDragged() {
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

void mouseReleased() {
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

void draw() {
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



void CheckPointsClicked() {
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

void CheckAddPoint() {
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