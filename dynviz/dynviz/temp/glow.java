import java.applet.*; import java.awt.*; import java.awt.image.*; import java.awt.event.*; import java.io.*; import java.net.*; import java.text.*; import java.util.*; import java.util.zip.*; import netscape.javascript.*; import javax.comm.*; import javax.sound.midi.*; import javax.sound.midi.spi.*; import javax.sound.sampled.*; import javax.sound.sampled.spi.*; import javax.xml.parsers.*; import javax.xml.transform.*; import javax.xml.transform.dom.*; import javax.xml.transform.sax.*; import javax.xml.transform.stream.*; import org.xml.sax.*; import org.xml.sax.ext.*; import org.xml.sax.helpers.*; public class glow extends BApplet {// GLOW
//by Alessandro Capozzo - GHOSTAGENCY.NET
// 6 November 2003
Ball [] cont;
float n=0.00f;
float q=0.03f;
int num=5;
float dens;
float nnn;
int nn;

void setup() {
  size(200,200);
  cont=new Ball[num];
  for (int index = 0; index < num; index++) {
    cont[index]=new Ball(10/((25.5f)*(index+1)),100,100,15);
  }

}

void loop() {
  background(250);
  n=n+0.1f;
  for (int index = 0; index < num; index++){
    cont[index].update();
  }

  for (int _x = 0; _x < width; _x++) {
    for (int _y = 0; _y < height; _y++) {
      dens = 0;
      for (int res = 0; res <num; res++) {
        dens +=cont[res].dim/(sqrt(sq(_x - cont[res].bx) + sq(_y - cont[res].by)));
      }
      nn=(int)((dens)*(150));
      nnn=50*( noise(_x*q,_y*q,n));

      int c=color(nn-nnn,(nn-nnn)*0.8f,(nn-nnn)*0.5f);
      pixels[_y*width+_x] =c;

    }
  }
}

class Ball {
  int bx,by;
  int difX,difY;
  int dim;
  float xSpeed,ySpeed,ndelay;

  Ball(float d,int initX,int initY,int magnitude){
    ndelay=d;
    bx=initX;
    by=initY;
    difX=0;
    difY=0;
    xSpeed=0.00f;
    ySpeed=0.00f;
    dim=magnitude;

  }
  void update (){
    difX=mouseX-bx;
    difY=mouseY-by;
    xSpeed+=difX*ndelay;
    ySpeed+=difY*ndelay;
    xSpeed*=.9f;
    ySpeed*=.9f;
    bx+=xSpeed;
    by+=ySpeed;

  }
}

}