package xander.woutzijnemptyactivity;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/*
    Class describes power ups
        Includes:
            * positions of the centers and the corners
            * the width and height
            * the ImageView itself
            * the type of power up

        There are methods implemented to set and get this data.
 */

public class PowerUp extends AppCompatActivity {
    //coordinates of the power ups
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

    //information off the power ups
    private int width;
    private int height;
    private ImageView powerUpImage;
    private int type;
    private boolean hittable=true;

    //constructor
    public PowerUp(int typeInput, ImageView powerUp){
        powerUpImage = powerUp;     //store the given image for the power up
        type = typeInput;

    }

    //This method sets the coordinates for the four corners
    public void setCorners() {
        //top left corner
        topLeftX=(centerX-(width/2));
        topLeftY=(centerY-(height/2));

        //top right corner
        topRightX=(centerX+(width/2));
        topRightY=(centerY-(height/2));

        //bottom right corner
        bottomRightX=(centerX+(width/2));
        bottomRightY=(centerY+(height/2));

        //bottom left corner
        bottomLeftX=(centerX-(width/2));
        bottomLeftY=(centerY+(height/2));
    }

    //make the power up go invisible
    public void setInvisible(){
        powerUpImage.setVisibility(View.GONE);
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
        width=powerUpImage.getWidth();
    }

    public void setHeight(){
        height=powerUpImage.getHeight();
    }

    public void setCenter(){
        int x[] = new int[2];
        powerUpImage.getLocationOnScreen(x);
        centerX=x[0]+(width/2);
        centerY=x[1]+(height/2);
    }

    public int getType(){
        return type;
    }

    public void setType(int typeInput){
        type=typeInput;
    }

    public void setHittable(boolean set_hittable){
        hittable=set_hittable;
    }

    public boolean getHittable(){
        return hittable;
    }


}