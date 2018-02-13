

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
  public color c;
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
  public color c;
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
  boolean contains(float x, float y) {
    return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
  }
}

class Circle {
  public int x, y, width, height;
  public color c;
  public Circle(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
  boolean contains(float x, float y) {
    return x >= this.x - width / 2.0f && x <= this.x + width / 2.0f && 
      y >= this.y - height / 2.0f && y <= this.y + height / 2.0f;
  }
  public void draw() {
    fill(c);
    ellipse(x, y, width, height);
  }
}