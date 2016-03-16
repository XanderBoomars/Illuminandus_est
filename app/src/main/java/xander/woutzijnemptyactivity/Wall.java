package xander.woutzijnemptyactivity;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/*
    Class describes the wall
        Includes:
            * positions of the centers and the corners
            * the width and height
            * the ImageView itself
            * the opacity of the wall

        There are methods implemented to set and get this data.
 */

public class Wall extends AppCompatActivity {
    //coordinates of the wall
    private int centerX;
    private int centerY;
    private int topLeftX;
    private int topRightX;
    private int bottomLeftX;
    private int bottomRightX;
    private int topLeftY;
    private int topRightY;
    private int bottomLeftY;
    private int bottomRightY;


    //info of the wall
    private int width;
    private int height;
    private float opacity;
    private ImageView wallImage;
    int timeTouched=0;

    //constructor
    public Wall(ImageView wall){
        wallImage = wall;
    }

    //This method sets the coordinates for the four corners
    public void setCorners() {
        if(width>height){
            //top left corner
            topLeftX=(centerX-(width/2)+2);
            topLeftY=(centerY-(height/2));

            //top right corner
            topRightX=(centerX+(width/2)-2);
            topRightY=(centerY-(height/2));

            //bottom right corner
            bottomRightX=(centerX+(width/2)-2);
            bottomRightY=(centerY+(height/2));

            //bottom left corner
            bottomLeftX=(centerX-(width/2)+2);
            bottomLeftY=(centerY+(height/2));
            return;
        }

        else{
            //top left corner
            topLeftX=(centerX-(width/2));
            topLeftY=(centerY-(height/2)+2);

            //top right corner
            topRightX=(centerX+(width/2));
            topRightY=(centerY-(height/2)+2);

            //bottom right corner
            bottomRightX=(centerX+(width/2));
            bottomRightY=(centerY+(height/2)-2);

            //bottom left corner
            bottomLeftX=(centerX-(width/2)+2);
            bottomLeftY=(centerY+(height/2)-2);
        }
    }

    public float getOpacity(){
        return opacity;
    }

    public void setOpacity(float i){
        opacity=i;
        wallImage.setAlpha(opacity);
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public int getCenterX(){
        return centerX;
    }

    public int getCenterY(){
        return centerY;
    }

    public int getTopLeftX(){
        return topLeftX;
    }

    public int getTopRightX(){
        return topRightX;
    }

    public int getBottomLeftX(){
        return bottomLeftX;
    }

    public int getBottomRightX(){
        return bottomRightX;
    }

    public int getTopLeftY(){
        return topLeftY;
    }

    public int getTopRightY(){
        return topRightY;
    }

    public int getBottomLeftY(){
        return bottomLeftY;
    }

    public int getBottomRightY(){
        return bottomRightY;
    }

    public void setWidth(){
        width=wallImage.getWidth();
    }

    public void setHeight(){
        height=wallImage.getHeight();
    }

    public void setCenter(){
        int x[] = new int[2];
        wallImage.getLocationOnScreen(x);
        centerX=x[0]+(width/2);
        centerY=x[1]+(height/2);
    }

    public void setTimeTouched(int timeInput){
        timeTouched=timeInput;
    }

    public int getTimeTouched(){
        return timeTouched;
    }
}