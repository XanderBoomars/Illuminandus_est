package xander.woutzijnemptyactivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sManager;
    private int a;      //stores x position for the layout of the ball
    private int b;      //stores y position for the layout of the ball
    int x = 0;          //stores the angle of the phone around the x-axis
    int y = 0;          //stores the angle of the phone around the y-axis

    boolean started = false;        //stores if the walls,ball and power ups have been set up already

    //ball storage info
    ImageView ballImage;
    Ball playingBall;
    boolean allowedMovement[] = {true, true, true, true};

    //power up storage info
    PowerUp powerUps[];
    ImageView powerUpImage[];
    int powerUpCounter[]=new int[8];

    //wall storage info
    Wall mazeWall[];
    int wallCounter[]= new int[8];

    //opacity settings
    float show = 1;
    float hide = 0;

    int timeStart;
    int time;
    int timeMinutes;
    int timeSeconds;
    TextView timeText;
    TextView powerUpsTouched;
    TextView parTimeText;
    TextView levelText;

    int visibleThreshold=10000;
    int invert=1;
    int pickup_range=500;
    int amountPowerUpsTouched=0;
    int parTime[]= new int[8];
    int parTimeMinutes;
    int parTimeSeconds;
    int levelNumber=0;  //level 1 equals levelNumber 0 etc


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level_1);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        timeText = (TextView) findViewById(R.id.timeText);
        parTimeText = (TextView) findViewById(R.id.ParText);
        powerUpsTouched = (TextView) findViewById(R.id.powerUpCounter);
        levelText = (TextView) findViewById(R.id.LevelText);



        //wall initializing
        wallCounter = getResources().getIntArray(R.array.wallCount);
        String wallName[] = getResources().getStringArray(R.array.wallName);

        int wallId;

        ImageView wallImage[] = new ImageView[wallCounter[levelNumber]];

        for (int i = 0; i < wallCounter[levelNumber]; i++) {
            wallId = getResources().getIdentifier(wallName[i], "id", getPackageName());
            wallImage[i]=(ImageView) findViewById(wallId);
        }


        mazeWall = new Wall[wallCounter[levelNumber]];
        for (int i = 0; i < wallCounter[levelNumber]; i++) {
            mazeWall[i] = new Wall(wallImage[i]);
        }

        for (int i = 4; i < wallCounter[levelNumber]; i++) {
            mazeWall[i].setOpacity(hide);
        }





        powerUpCounter=getResources().getIntArray(R.array.powerUpCount);
        powerUps = new PowerUp[powerUpCounter[levelNumber]];
        powerUpImage = new ImageView[powerUpCounter[levelNumber]];

        //power up initializing
        powerUpImage[0] = (ImageView) findViewById(R.id.power_up_0);
        powerUpImage[1] = (ImageView) findViewById(R.id.power_up_1);
        powerUpImage[2] = (ImageView) findViewById(R.id.power_up_2);

        powerUps[0] = new PowerUp(1, powerUpImage[0]);
        powerUps[1] = new PowerUp(3, powerUpImage[1]);
        powerUps[2] = new PowerUp(4, powerUpImage[2]);


        //ball initializing
        ballImage = (ImageView) findViewById(R.id.ball);
        playingBall = new Ball(ballImage);

        //hide top bar
        getSupportActionBar().hide();

        parTime=getResources().getIntArray(R.array.parTime);

        parTimeMinutes=(parTime[levelNumber]/(1000*60))%60;
        parTimeSeconds=((parTime[levelNumber]-(parTimeMinutes*60*1000))/1000)%60;
        parTimeText.setText("Par time: "+Integer.toString(parTimeMinutes)+":"+Integer.toString(parTimeSeconds));

        levelText.setText("Level "+Integer.toString(levelNumber+1));

        timeStart=(int)System.currentTimeMillis();
    }


    //when this Activity starts
    @Override
    protected void onResume() {
        super.onResume();
        /*register the sensor listener to listen to the gyroscope sensor, use the
        callbacks defined in this class, and gather the sensor information as quick
        as possible*/
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    //When this Activity isn't visible anymore
    @Override
    protected void onStop() {
        //unregister the sensor listener
        sManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        //Do nothing.
    }

    /*
        This method is called when a wall has been hit.
        It will then analyze from which way the ball came in relation to the wall it hit.
        With that information it stores which movements are now still allowed (up, down, right or left).
        This is done via the Minkowski addition formula: https://en.wikipedia.org/wiki/Minkowski_addition

        This method receives a ball and a wall entity.
        It returns nothing

        It changes the allowedMovement boolean
    */

    public void limitMovement(Ball ball, Wall wall) {
        float wy = (ball.getWidth() + wall.getWidth()) * (ball.getCenterY() - wall.getCenterY());
        float hx = (ball.getHeight() + wall.getHeight()) * (ball.getCenterX() - wall.getCenterX());

        wall.setOpacity(show);
        wall.setTimeTouched((int) System.currentTimeMillis()-timeStart);

        if (wy > hx) {
            if (wy > -hx) {//top
                allowedMovement[1] = false;     //block downwards motion
            } else {//left
                allowedMovement[2] = false;     //block rightwards motion
            }
        } else {
            if (wy > -hx) {//right
                allowedMovement[3] = false;     //block leftwards motion
            } else {//bottom
                allowedMovement[0] = false;     //block upwards motion
            }
        }
    }

    /*
        This methods determines if a wall is hit by the ball.
        If this is the case then it will make that wall visible and it will call the limitMovement() method to determine which movement should be blocked.

        This method receives a ball and wall entity.
        It returns nothing

        It changes opacity of the wall that is hit
    */

    public void intersectWall(Ball ball, Wall wall) {
        //top left corner of the ball
        if (ball.getTopLeftX() >= wall.getTopLeftX() && ball.getTopLeftX() <= wall.getTopRightX()) {                     //is the x position of the ball between those of the two sides of the wall
            if (ball.getTopLeftY() >= wall.getTopLeftY() && ball.getTopLeftY() <= wall.getBottomLeftY()) {               //is the y position of the ball between those of the two sides of the wall
                limitMovement(ball, wall);
                return;
            }
        }

        if(wall.getTopLeftX() >= ball.getTopLeftX() && wall.getTopLeftX() <= ball.getTopRightX() ){
            if (wall.getTopLeftY() >= ball.getTopLeftY() && wall.getTopLeftY() <= ball.getBottomLeftY()) {
                limitMovement(ball, wall);
                return;
            }
        }




        //top rigth corner of the ball
        if (ball.getTopRightX() >= wall.getTopLeftX() && ball.getTopRightX() <= wall.getTopRightX()) {                   //is the x position of the ball between those of the two sides of the wall
            if (ball.getTopRightY() >= wall.getTopLeftY() && ball.getTopRightY() <= wall.getBottomLeftY()) {             //is the y position of the ball between those of the two sides of the wall
                limitMovement(ball, wall);
                return;
            }
        }

        if (wall.getTopRightX() >= ball.getTopLeftX() && wall.getTopRightX() <= ball.getTopRightX()) {
            if (wall.getTopRightY() >= ball.getTopLeftY() && wall.getTopRightY() <= ball.getBottomLeftY()) {
                limitMovement(ball, wall);
                return;
            }
        }




        //bottom left corner of the ball
        if (ball.getBottomLeftX() >= wall.getBottomLeftX() && ball.getBottomLeftX() <= wall.getBottomRightX()) {        //is the x position of the ball between those of the two sides of the wall
            if (ball.getBottomLeftY() >= wall.getTopLeftY() && ball.getBottomLeftY() <= wall.getBottomLeftY()) {        //is the y position of the ball between those of the two sides of the wall
                limitMovement(ball, wall);
                return;
            }
        }

        if (wall.getBottomLeftX() >= ball.getBottomLeftX() && wall.getBottomLeftX() <= ball.getBottomRightX()) {        //is the x position of the ball between those of the two sides of the wall
            if (wall.getBottomLeftY() >= ball.getTopLeftY() && wall.getBottomLeftY() <= ball.getBottomLeftY()) {        //is the y position of the ball between those of the two sides of the wall
                limitMovement(ball, wall);
                return;
            }
        }





        //bottom rigth corner of the ball
        if (ball.getBottomRightX() >= wall.getBottomLeftX() && ball.getBottomRightX() <= wall.getBottomRightX()) {      //is the x position of the ball between those of the two sides of the wall
            if (ball.getBottomRightY() >= wall.getTopLeftY() && ball.getBottomRightY() <= wall.getBottomLeftY()) {      //is the y position of the ball between those of the two sides of the wall
                limitMovement(ball, wall);
                return;
            }
        }

        if (wall.getBottomRightX() >= ball.getBottomLeftX() && wall.getBottomRightX() <= ball.getBottomRightX()) {      //is the x position of the ball between those of the two sides of the wall
            if (wall.getBottomRightY() >= ball.getTopLeftY() && wall.getBottomRightY() <= ball.getBottomLeftY()) {      //is the y position of the ball between those of the two sides of the wall
                limitMovement(ball, wall);
            }
        }

    }

    /*
        This methods determines if a power up is hit by the ball.
        If this is the case then it will call the hitPowerUp() method, which determines what should happen.

        This method receives a ball and powerUp entity.
        It returns nothing

        It changes nothing.
     */
    public void intersectPowerUp(Ball ball, PowerUp powerUp) {
        //top left corner of the ball
        if (ball.getTopLeftX() >= powerUp.getTopLeftX() && ball.getTopLeftX() <= powerUp.getTopRightX()) {              //is the x position of the ball between those of the two sides of the power up
            if (ball.getTopLeftY() >= powerUp.getTopLeftY() && ball.getTopLeftY() <= powerUp.getBottomLeftY()) {        //is the y position of the ball between those of the two sides of the power up
                hitPowerUp(powerUp);
                return;
            }
        }

        //top rigth corner of the ball
        if (ball.getTopRightX() >= powerUp.getTopLeftX() && ball.getTopRightX() <= powerUp.getTopRightX()) {            //is the x position of the ball between those of the two sides of the power up
            if (ball.getTopRightY() >= powerUp.getTopLeftY() && ball.getTopRightY() <= powerUp.getBottomLeftY()) {      //is the y position of the ball between those of the two sides of the power up
                hitPowerUp(powerUp);
                return;
            }
        }

        //bottom left corner of the ball
        if (ball.getBottomLeftX() >= powerUp.getBottomLeftX() && ball.getBottomLeftX() <= powerUp.getBottomRightX()) {  //is the x position of the ball between those of the two sides of the power up
            if (ball.getBottomLeftY() >= powerUp.getTopLeftY() && ball.getBottomLeftY() <= powerUp.getBottomLeftY()) {  //is the y position of the ball between those of the two sides of the power up
                hitPowerUp(powerUp);
                return;
            }
        }

        //bottom rigth corner of the ball
        if (ball.getBottomRightX() >= powerUp.getBottomLeftX() && ball.getBottomRightX() <= powerUp.getBottomRightX()) {//is the x position of the ball between those of the two sides of the power up
            if (ball.getBottomRightY() >= powerUp.getTopLeftY() && ball.getBottomRightY() <= powerUp.getBottomLeftY()) {//is the y position of the ball between those of the two sides of the power up
                hitPowerUp(powerUp);
            }
        }
    }

    /*
        This method determines what should happen if a power up is hit

        It receives a powerUp entity
        It returns nothing

        It changes the opacity of the powerUp
     */
    public void hitPowerUp(PowerUp powerUp){
        if(powerUp.getHittable()) {
            amountPowerUpsTouched = amountPowerUpsTouched + 1;
            powerUpsTouched.setText(Integer.toString(amountPowerUpsTouched) + "/" + Integer.toString(powerUpCounter[levelNumber]));
        }

        if(powerUp.getType()==1 && powerUp.getHittable()){
            pinkPowerUp(powerUp);
        }

        if(powerUp.getType()==2 && powerUp.getHittable()){
            greenPowerUp(powerUp);
        }

        if (powerUp.getType()==3 && powerUp.getHittable()){
            redPowerUp(powerUp);
        }

        if(powerUp.getType()==4 && powerUp.getHittable()){
            bluePowerUp(powerUp);
        }


    }

    public void pinkPowerUp(PowerUp powerUp){
        for(int i=0; i<wallCounter[levelNumber];i++){
            mazeWall[i].setOpacity(show);
            mazeWall[i].setTimeTouched((int)System.currentTimeMillis()-timeStart);
        }
        powerUp.setInvisible();
        powerUp.setHittable(false);
    }

    public void greenPowerUp(PowerUp powerUp){

        powerUp.setInvisible();
        powerUp.setHittable(false);

    }

    public void redPowerUp(PowerUp powerUp){
        invert = -1;
        powerUp.setInvisible();
        powerUp.setHittable(false);
    }

    public void bluePowerUp(PowerUp powerUp){
        for(int i=4;i<wallCounter[levelNumber];i++){
            double deltaDsquared = Math.pow(mazeWall[i].getCenterX() - powerUp.getCenterX(), 2) + Math.pow(mazeWall[i].getCenterY() - powerUp.getCenterY(), 2);
            if(deltaDsquared < Math.pow(pickup_range, 2)){
                mazeWall[i].setOpacity(show);
                mazeWall[i].setTimeTouched((int) System.currentTimeMillis());
            }
        }


        powerUp.setInvisible();
        powerUp.setHittable(false);
    }





    /*
        This method is called when there is a valid change in the values that are measured by the gyroscope.

        The steps that this method takes are quite complicated.
            The first step that is taken is to determine how may steps should be taken in the x and y direction.
            It then keeps score of how many steps have been taken in both the x and y direction.
            If this below the steps that have to be taken then the while loop will go through another cycle.
            This loop moves the ball one pixel in both directions (if this is still necessary and a movement in that direction is still allowed) and then checks if there is a collision.
            To detect this the intersectWall() method is called.
            Right after this the intersectPowerUp() method is called to detect if a power up has been hit.
            When this is all done the new position will be send to ImageView of the ball by setLayoutParams() method.

        This method receives two integers, one for the angle in the x direction and one for the y direction.
        It returns nothing

        It changes the following:
            * the layout of the ball
            * the offset from the side and top
     */

    public void move(int x, int y) {
        RelativeLayout.LayoutParams alp = playingBall.getLayoutParams();
        int maxMovementX = Math.abs(x);
        int maxMovenentY = Math.abs(y);
        int stepsTakenX = 0;
        int stepsTakenY = 0;
        a = alp.leftMargin;
        b = alp.topMargin;

        while (maxMovementX > stepsTakenX || maxMovenentY > stepsTakenY) {
            //up 0, down 1, right 3, left 2
            for (int i = 0; i < wallCounter[levelNumber]; i++) {
                intersectWall(playingBall, mazeWall[i]);
            }

            for(int i=0; i< powerUpCounter[levelNumber]; i++){
                intersectPowerUp(playingBall, powerUps[i]);
            }

            if (stepsTakenX < maxMovementX) {
                stepsTakenX = stepsTakenX + 1;
                if (x > 0 && allowedMovement[3]) {//right
                    playingBall.setCenterX(playingBall.getCenterX() - 1);
                    playingBall.setCorners();
                    a = a - 1;
                }
                if (x < 0 && allowedMovement[2]) {//left
                    playingBall.setCenterX(playingBall.getCenterX() + 1);
                    playingBall.setCorners();
                    a = a + 1;
                }
            }

            if (stepsTakenY < maxMovenentY) {
                stepsTakenY = stepsTakenY + 1;
                if (y > 0 && allowedMovement[1]) {//down
                    playingBall.setCenterY(playingBall.getCenterY() - 1);
                    playingBall.setCorners();
                    b = b - 1;
                }
                if (y < 0 && allowedMovement[0]) {//up
                    playingBall.setCenterY(playingBall.getCenterY() + 1);
                    playingBall.setCorners();
                    b = b + 1;
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            allowedMovement[i] = true;
        }

        alp.leftMargin = a;
        alp.topMargin = b;
        playingBall.setLayoutParams(alp);
    }

    /*
        This method sets all the data for the power ups, walls and the balls.
        This can not be done in the onCreate method because the layout has not been initialized there.

        It receives nothing
        It returns nothing

        It changes the same for all the objects:
            * Width
            * Height
            * Centers
            * Corners
     */

    public void start() {
        for(int i=0; i< powerUpCounter[levelNumber]; i++){
            powerUps[i].setWidth();
            powerUps[i].setHeight();
            powerUps[i].setCenter();
            powerUps[i].setCorners();
        }
        for (int i = 0; i < wallCounter[levelNumber]; i++) {
            mazeWall[i].setWidth();
            mazeWall[i].setHeight();
            mazeWall[i].setCenter();
            mazeWall[i].setCorners();
        }
        playingBall.setWidth();
        playingBall.setHeight();
        playingBall.setCenter();
        playingBall.setCorners();

        powerUpsTouched.setText(Integer.toString(amountPowerUpsTouched)+"/"+Integer.toString(powerUpCounter[levelNumber]));
        started = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (started == false) {
            start();
        }

        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        //adjusts and then saves the angle of phone in the x and y direction.
        x = Math.round(event.values[1]) / 3 * invert;
        y = Math.round(-event.values[2]) / 3 * invert;

        //limits the speed
        if (x > 15) {
            x = 15;
        }
        if (x < -15) {
            x = -15;
        }
        if (y > 15) {
            y = 15;
        }
        if (y < -15) {
            y = -15;
        }


        time = (int) (System.currentTimeMillis() - timeStart);
        for (int i = 4; i < wallCounter[levelNumber]; i++) {
            if (time - mazeWall[i].getTimeTouched() > visibleThreshold) {
                mazeWall[i].setOpacity(hide);
            }
        }

        timeMinutes = (time / (1000 * 60)) % 60;
        timeSeconds = ((time - (timeMinutes * 60 * 1000)) / 1000) % 60;

        if (timeMinutes < 10) {
            if (timeSeconds < 10) {
                timeText.setText("0" + Integer.toString(timeMinutes) + ":0" + Integer.toString(timeSeconds));
            } else {
                timeText.setText("0" + Integer.toString(timeMinutes) + ":" + Integer.toString(timeSeconds));
            }
        } else {
            if (timeSeconds < 10) {
                timeText.setText(Integer.toString(timeMinutes) + ":0" + Integer.toString(timeSeconds));
            } else {
                timeText.setText(Integer.toString(timeMinutes) + ":" + Integer.toString(timeSeconds));
            }
        }

        if (time > (parTime[levelNumber] + 1000)) {
            timeText.setTextColor(getResources().getColor(R.color.red));
        }

        move(x, y);

    }



}