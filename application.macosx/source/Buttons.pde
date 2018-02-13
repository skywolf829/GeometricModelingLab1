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