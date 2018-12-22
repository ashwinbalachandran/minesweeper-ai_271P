/*

AUTHOR:      John Lu

DESCRIPTION: This file contains your agent class, which you will
             implement.

NOTES:       - If you are having trouble understanding how the shell
               works, look at the other parts of the code, as well as
               the documentation.

             - You are only allowed to make changes to this portion of
               the code. Any changes to other portions of the code will
               be lost when the tournament runs your code.
 */

package src;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import src.Action.ACTION;

public class MyAI extends AI {
	// ########################## INSTRUCTIONS ##########################
	// 1) The Minesweeper Shell will pass in the board size, number of mines
	// 	  and first move coordinates to your agent. Create any instance variables
	//    necessary to store these variables.
	//
	// 2) You MUST implement the getAction() method which has a single parameter,
	// 	  number. If your most recent move is an Action.UNCOVER action, this value will
	//	  be the number of the tile just uncovered. If your most recent move is
	//    not Action.UNCOVER, then the value will be -1.
	//
	// 3) Feel free to implement any helper functions.
	//
	// ###################### END OF INSTURCTIONS #######################

	// This line is to remove compiler warnings related to using Java generics
	// if you decide to do so in your implementation.
	int r,c,numMines,x,y;
	static String gBoard[][];
	static String gBoardEndGame[][];
	static float gBoardProbability[][];
	static String [][]testarray;
	//	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	HashMap <String,Integer> zeroes=new HashMap<String, Integer>();
	HashMap <String,Integer> oneLoc=new HashMap<String, Integer>();
	int ones[][]=new int[3][3];
	static int prevX, prevY,moves,surround,centerX,centerY,top,sZero,zX,zY,tempX,tempY,flagCenter,bombay,deathCounter,maxMass,updateCounter;
	static boolean panic = false,stageOne = false,stageTwo=false,stageFour=false,stageFive=false;
	static int row,col,globalNullCounter;
	static Action actionToBeTaken;
	static int safeX,safeY;
	static HashMap <Integer,String> cords = new HashMap<Integer, String>();
	static int cordCounter;
	int surroundCells[][] = { { -1,-1 },{ -1,0 },{ -1,1 },{ 0,1 },{ 1,1 },{ 1,0 },{ 1,-1 } ,{ 0,-1 } };
	int oneCell[][]= {{0,0},{0,1},{0,2},{1,0},{1,2},{2,0},{2,1},{2,2}};
	public MyAI(int rowDimension, int colDimension, int totalMines, int startX, int startY) {
		// ################### Implement Constructor (required) ####################
		r=rowDimension;
		c=colDimension;
		numMines=totalMines;
		gBoard=new String[r][c];
		gBoardEndGame=new String[r][c];
		gBoardProbability=new float[r][c];
		testarray=new String[r][c];
		cords = new HashMap<Integer, String>();
		x=startX;
		maxMass=0;
		y=startY;
		centerX=tempX=x;
		safeX=y;
		safeY=x;
		centerY=tempY=y;
		prevX=row = r-y;
		prevY=col = x-1;
		moves=0;
		surround=0;
		top=-1;
		sZero=0;
		flagCenter=0;
		bombay=numMines;
		cordCounter=0;
		stageOne = false;
		stageTwo=false;
		panic = false;
		deathCounter=0;
		updateCounter=0;
		globalNullCounter=0;
	}

	// ################## Implement getAction(), (required) #####################
	public Action getAction(int number) {
		if(maxMass<number)
			maxMass=number;
		if(moves>2 * c* r)
			return new Action(ACTION.LEAVE);
		moves++;
		if(!stageOne)
			gBoard[r-tempY][tempX-1]=number+"";
		else {
			if(safeX==0 && safeY==0)
				gBoard[r-tempY][tempX-1]=number+"";
			else
				gBoard[r-safeY][safeX-1]=number+"";
		}
		if(number==0) {
			if(zeroes.containsKey(tempX+","+tempY))
				top=top-1+1;
			else {
				if(moves!=1)
					zeroes.put(tempX+","+tempY,0);
			}
		}
		if(number==1) {
			ones[oneCell[sZero][0]][oneCell[sZero][1]]=1;
			sZero=(sZero+1)%8;
			if(!oneLoc.containsKey(tempX+","+tempY))
				oneLoc.put(tempX+","+tempY,1);
		}
		if(moves>1 && panic && zeroes.size()>0)
			shiftCenter();
		if(moves==1) {
			if(!gBoard[r-y][x-1].equals("0"))
				panic=true;
		}
		if(surround<8 && !stageOne) {
			openSurrounding();
		}
		else if(!stageOne){
			surround=0;
			shiftCenter();
		}
		if(!stageOne) {
			row=r-tempY;
			col=tempX-1;
			if(gBoard[row][col]!=null)
				openSurrounding();
			if(tempX<1 || tempX>r||tempY<1||tempY>c)
				stageOne = true;
			else
				return new Action(ACTION.UNCOVER,tempX,tempY);
		}
		if(stageOne) {
			while(cords.size()>0) {
				String tamper[] = cords.get(cordCounter-1).split(",");
				safeX = Integer.parseInt(tamper[1])+1;
				safeY = r-Integer.parseInt(tamper[0]);
				cords.remove(cordCounter-1);
				cordCounter--;
				return actionToBeTaken = new Action(ACTION.UNCOVER,safeX,safeY);
			}
			stageTwo();
			if(cords.size()>0) {
				String tamper[] = cords.get(cordCounter-1).split(",");
				safeX = Integer.parseInt(tamper[1])+1;
				safeY = r-Integer.parseInt(tamper[0]);
				cords.remove(cordCounter-1);
				cordCounter--;
				actionToBeTaken = new Action(ACTION.UNCOVER,safeX,safeY);
			}
			else {
				stageThree(gBoard);
				actionToBeTaken = new Action(ACTION.UNCOVER,safeX,safeY);
			}
		}
		//		System.out.println("No of moohves="+moves);
		if(null!=gBoard[r-safeY][safeX-1]) {
			do {
				if(deathCounter>2) {
					if(stageFour()) {
						deathCounter=0;
						break;
					}
				}
				else {
					row=r-safeY;
					col=safeX-1;
					int oldX=safeX;
					int oldY=safeY;
					stageThree(gBoard);
					if(oldX==safeX && oldY==safeY)
						++deathCounter;
					else
						deathCounter=0;
				}
			}while(gBoard[row][col]!=null && nullCounter()!=0);
		}
		return actionToBeTaken;
	}

	private int nullCounter() {
		@SuppressWarnings("unused")
		int bomb=0;
		int lnx=0,lny=0;
		int nullCounter=0;
		for(int i=0;i<r;i++)
			for(int j=0;j<c;j++) {
				if(null==gBoard[i][j]) {
					++nullCounter;
					lnx=i;
					lny=j;
				}
				else if(gBoard[i][j].equals("B"))
					++bomb;
			}
		if(bomb==numMines && nullCounter!=0) {
			safeX=lny+1;
			safeY=r-lnx;
			actionToBeTaken=new Action(ACTION.UNCOVER,safeX,safeY);
		}
		if(globalNullCounter!=nullCounter)
			globalNullCounter=nullCounter;
		else {
			safeX=lny+1;
			safeY=r-lnx;
			actionToBeTaken=new Action(ACTION.UNCOVER,safeX,safeY);
		}
		return nullCounter;
	}

	private boolean stageFour() {
		copyBoardState();
		updateBoardState();
		stageFour=true;
		int ctr=0;
		do {
			ctr++;
			row=r-safeY;
			col=safeX-1;
			stageThree(gBoardEndGame);
			if(gBoard[row][col]==null) {
				actionToBeTaken=new Action(ACTION.UNCOVER,safeX,safeY);
				return true;
			}
		}while(ctr<1);
		stageFive=stageFive();
		if(stageFive)
			stageThree(gBoard);
		return stageFive;
	}

	private boolean stageFive() {
		int sX,sY;
		boolean status = false;
		String []maxCords=new String[2];
		int nullCounter=0;
		for(int i=0;i<r;i++)
			for(int j=0;j<c;j++) {
				if(null!=gBoard[i][j]) {
					nullCounter=0;
					for(int k=0;k<8;k++) {
						sX=surroundCells[k][0]+i;
						sY=surroundCells[k][1]+j;
						if(sX>=r||sY>=c||sX<0||sY<0)
							continue;
						else
							if(null==gBoard[sX][sY]) {
								++nullCounter;
							}
					}
					if(nullCounter==0)
						continue;
				}
				else {
					calculateProbability(i,j);
					maxCords=findHighestProbability().split(",");
					if(maxCords[0].equals("-1")||maxCords[1].equals("-1"))
						continue;
					else {
						status = true;
					}
				}
			}
		if(status)
			gBoard[Integer.parseInt(maxCords[0])][Integer.parseInt(maxCords[1])]="B";
		return status;
	}

	private String findHighestProbability() {
		float max=0f;
		int mX=-1,mY=-1;
		for(int i=0;i<r;i++)
			for(int j=0;j<c;j++) {
				if(Float.compare(gBoardProbability[i][j],0.0f)!=0)
					if(Float.compare(gBoardProbability[i][j],max)>0) {
						max=gBoardProbability[i][j];
						mX=i;
						mY=j;
					}
			}
		String cords=mX+","+mY;
		return cords;
	}

	private void calculateProbability(int pX, int pY) {
		int sX,sY,b,n;
		for(int k=0;k<8;k++) {
			sX=surroundCells[k][0]+pX;
			sY=surroundCells[k][1]+pY;
			if((sX>=r||sY>=c||sX<0||sY<0))
				continue;
			else{
				if((null!=gBoard[sX][sY]&&!gBoard[sX][sY].equals("B"))) {
					b=Integer.parseInt(gBoard[sX][sY])-searchSurrounding(sX, sY);
					n=countNulls(sX,sY);
					if(n>0)
						gBoardProbability[pX][pY]+=(float)(factorial(n-1)/(factorial(b-1)*factorial(n-1-b-1)))/(factorial(n)/(factorial(b)*factorial(n-b)));
				}
			}
		}
	}
	private int countNulls(int nX, int nY) {
		int nullChecker=0,sX,sY;
		for(int i=0;i<8;i++) {
			sX=surroundCells[i][0]+nX;
			sY=surroundCells[i][1]+nY;
			if(sX>=r||sY>=c||sX<0||sY<0)
				continue;
			else
				if(null==gBoard[sX][sY]) {
					++nullChecker;
				}
		}
		return nullChecker;
	}

	private int factorial(int f) {
		int fact=1;
		if(f==0)
			return 1;
		for(int i=1;i<=f;i++)
			fact*=i;
		return fact;
	}
	private void updateBoardState() {
		updateCounter++;
		int bomb=0;
		for(int i=0;i<r;i++)
			for(int j=0;j<c;j++) {
				if(null!=gBoard[i][j]&&!gBoard[i][j].equals("0")&&!gBoard[i][j].equals("B")) {
					bomb=searchSurrounding(i,j);
					//Update cell as follows cell=cell-bomb;
					if(bomb!=0) {
						if(Integer.parseInt(gBoardEndGame[i][j])-bomb>0)
							gBoardEndGame[i][j]=(Integer.parseInt(gBoardEndGame[i][j])-bomb)+"";
					}
				}
			}
	}

	private int searchSurrounding(int x, int y) {
		//Loop surrounding and count bombs
		int bamb=0;
		int nullChecker=0;
		int sX,sY;
		for(int i=0;i<8;i++) {
			sX=surroundCells[i][0]+x;
			sY=surroundCells[i][1]+y;
			if(sX>=r||sY>=c||sX<0||sY<0)
				continue;
			else
				if(null==gBoard[sX][sY]) {
					++nullChecker;
				}
				else {
					if(gBoard[sX][sY].equals("B"))
						bamb++;
				}
		}
		if(nullChecker==0)
			bamb=0;
		return bamb;
	}

	private void copyBoardState() {
		for(int i=0;i<r;i++)
			for(int j=0;j<c;j++) {
				if(null==gBoard[i][j])
					gBoardEndGame[i][j]=null;
				else
					gBoardEndGame[i][j]=gBoard[i][j];
			}
	}

	private void stageThree(String [][]gBoard) {
		int x3;
		int y1,y2,y3,y4;
		//Right
		for(int i=0;i<gBoard.length;i++)
			for(int j=0;j<gBoard[0].length;j++) {
				y1=j;
				y2=j+1;
				y3=j+2;
				y4=j+3;
				x3=i-1;
				if(y2>=c||y3>=c||x3<0||null==gBoard[i][y1]||null==gBoard[i][y2]||null==gBoard[i][y3])
					continue;
				if(gBoard[i][y1].equals("1") && gBoard[i][y2].equals("1")&&gBoard[i][y3].equals("1") &&(y4>=c||(y4<c&&null!=gBoard[i][y4]&&gBoard[i][y4].equals("1")))) {
					if(null==gBoard[x3][y3]) {
						if(((y1-1)<0)||((y1-1)>-1&&(i-1)>-1&&null!=gBoard[i-1][y1-1])) {
							stageTwo = true;
							safeX=y3+1;
							safeY=r-x3;
							stageTwo();
							return;
						}
						else
							continue;
					}
					else {
						if((i+1)<r) {
							if(null!=gBoard[i+1][y3])
								continue;
							else if(((y1-1)<0)||((y1-1)>-1&&(i+1)<r&&null!=gBoard[i+1][y1-1])) {
								stageTwo=true;
								safeX=y3+1;
								safeY=r-(i+1);
								stageTwo();
								return;
							}
							else
								continue;
						}
					}
				}
				if(gBoard[i][y1].equals("1")&&gBoard[i][y2].equals("2")&&gBoard[i][y3].equals("1")) {
					if(null==gBoard[x3][y1] && null==gBoard[x3][y3]) {
						stageTwo = true;
						MyAI.gBoard[x3][y1]="B";
						MyAI.gBoard[x3][y3]="B";
						if(stageFour==true) {
							MyAI.gBoardEndGame[x3][y1]="B";
							MyAI.gBoardEndGame[x3][y3]="B";
						}
						bombay-=2;
						stageTwo();
					}
					else {
						if((i+1)<r) {
							if(null!=gBoard[i+1][y1]&&null!=gBoard[i+1][y3])
								continue;
							stageTwo=true;
							MyAI.gBoard[i+1][y1]="B";
							MyAI.gBoard[i+1][y3]="B";
							if(stageFour==true) {
								MyAI.gBoardEndGame[i+1][y1]="B";
								MyAI.gBoardEndGame[i+1][y3]="B";
							}
							bombay-=2;
							stageTwo();
						}
					}
				}
				if(!(y4>=c)&&!(null==gBoard[i][y4])&&gBoard[i][y1].equals("1") && gBoard[i][y2].equals("2")&&"2".equals(gBoard[i][y3])&&("2".equals(gBoard[i][y4])||"1".equals(gBoard[i][y4]))) {
					if(null==gBoard[x3][y2] && null==gBoard[x3][y3]) {
						stageTwo = true;
						MyAI.gBoard[x3][y2]="B";
						MyAI.gBoard[x3][y3]="B";
						if(stageFour==true) {
							MyAI.gBoardEndGame[x3][y2]="B";
							MyAI.gBoardEndGame[x3][y3]="B";
						}
						bombay-=2;
						stageTwo();
					}
					else {
						if((i+1)<r) {
							if(null!=gBoard[i+1][y2]&&null!=gBoard[i+1][y3])
								continue;
							stageTwo=true;
							MyAI.gBoard[i+1][y2]="B";
							MyAI.gBoard[i+1][y3]="B";
							if(stageFour==true) {
								MyAI.gBoardEndGame[i+1][y2]="B";
								MyAI.gBoardEndGame[i+1][y3]="B";
							}
							bombay-=2;
							stageTwo();
						}
					}
				}
				if(gBoard[i][y1].equals("1")&&gBoard[i][y2].equals("2")&&gBoard[i][y3].equals("B")) {
					if(null==gBoard[x3][y3]) {
						stageTwo=true;
						safeX=y3+1;
						safeY=r-x3;
						stageTwo();
						return;
					}
					else {
						if((x3+2)<r) {
							if(null!=gBoard[x3+2][y3])
								continue;
							stageTwo=true;
							safeX=y3+1;
							safeY=r-(x3+2);
							stageTwo();
							return;
						}
					}
				}

			}
		//Left
		for(int i=0;i<gBoard.length;i++)
			for(int j=gBoard[0].length-1;j>=0;j--) {
				y1=j;
				y2=j-1;
				y3=j-2;
				y4=j-3;
				x3=i-1;
				if(y2<0||y3<0||x3<0||null==gBoard[i][y1]||null==gBoard[i][y2]||null==gBoard[i][y3])
					continue;
				if(gBoard[i][y1].equals("1") && gBoard[i][y2].equals("1")&&gBoard[i][y3].equals("1")&&(y4<0||(y4>=0 && null!=gBoard[i][y4]&&gBoard[i][y4].equals("1")))) {
					if(null==gBoard[x3][y3]) {
						if(((y1+1)>=c)||((y1+1)<c&&(i-1)>-1&&null!=gBoard[i-1][y1+1])) {
							stageTwo = true;
							safeX=y3+1;
							safeY=r-x3;
							stageTwo();
							return;
						}
						else
							continue;
					}
					else {
						if((i+1)<r) {
							if(null!=gBoard[i+1][y3])
								continue;
							else if(((y1+1)>=c)||((y1+1)<c&&(i+1)<r&&null!=gBoard[i+1][y1+1])) {
								stageTwo=true;
								safeX=y3+1;
								safeY=r-(i+1);
								stageTwo();
								return;
							}
							else
								continue;
						}
					}
				}
				if(gBoard[i][y1].equals("1")&&gBoard[i][y2].equals("2")&&gBoard[i][y3].equals("1")) {
					if(null==gBoard[x3][y1] && null==gBoard[x3][y3]) {
						stageTwo = true;
						MyAI.gBoard[x3][y1]="B";
						MyAI.gBoard[x3][y3]="B";
						if(stageFour==true) {
							MyAI.gBoardEndGame[x3][y1]="B";
							MyAI.gBoardEndGame[x3][y3]="B";
						}
						bombay--;
						stageTwo();
					}
					else {
						if((i+1)<r) {
							if(null!=gBoard[i+1][y1]&&null!=gBoard[i+1][y3])
								continue;
							stageTwo = true;
							MyAI.gBoard[i+1][y1]="B";
							MyAI.gBoard[i+1][y3]="B";
							if(stageFour==true) {
								MyAI.gBoardEndGame[i+1][y1]="B";
								MyAI.gBoardEndGame[i+1][y3]="B";
							}
							bombay-=2;
							stageTwo();
						}
					}
				}
				if(!(y4<0)&&!(null==gBoard[i][y4])&&gBoard[i][y1].equals("1") && gBoard[i][y2].equals("2")&&gBoard[i][y3].equals("2")&&("2".equals(gBoard[i][y4])||"1".equals(gBoard[i][y4]))) {
					if(null==gBoard[x3][y2] && null==gBoard[x3][y3]) {
						stageTwo = true;
						MyAI.gBoard[x3][y2]="B";
						MyAI.gBoard[x3][y3]="B";
						if(stageFour==true) {
							MyAI.gBoardEndGame[x3][y2]="B";
							MyAI.gBoardEndGame[x3][y3]="B";
						}
						bombay--;
						stageTwo();
					}
					else {
						if((i+1)<r) {
							if(null!=gBoard[i+1][y2]&&null!=gBoard[i+1][y3])
								continue;
							stageTwo = true;
							MyAI.gBoard[i+1][y2]="B";
							MyAI.gBoard[i+1][y3]="B";
							if(stageFour==true) {
								MyAI.gBoardEndGame[i+1][y2]="B";
								MyAI.gBoardEndGame[i+1][y3]="B";
							}
							bombay-=2;
							stageTwo();
						}
					}
				}
				if(gBoard[i][y1].equals("1")&&gBoard[i][y2].equals("2")&&gBoard[i][y3].equals("B")) {
					if(null==gBoard[x3][y3]) {
						stageTwo=true;
						safeX=y3+1;
						safeY=r-x3;
						stageTwo();
						return;
					}
					else {
						if(((x3+2)<r)) {
							if(null!=gBoard[x3+2][y3])
								continue;
							stageTwo=true;
							safeX=y3+1;
							safeY=r-(x3+2);
							stageTwo();
							return;
						}
					}
				}
			}
		//Down
		for(int i=0;i<gBoard[0].length;i++)
			for(int j=0;j<gBoard.length;j++) {
				y1=j;
				y2=j+1;
				y3=j+2;
				y4=j+3;
				x3=i-1;
				if(y2>=r||y3>=r||x3<0||null==gBoard[y1][i]||null==gBoard[y2][i]||null==gBoard[y3][i])
					continue;
				if(gBoard[y1][i].equals("1") && gBoard[y2][i].equals("1")&&gBoard[y3][i].equals("1")&&(y4>=r||(y4<r&&null!=gBoard[y4][i]&&gBoard[y4][i].equals("1")))) {
					if(null==gBoard[y3][x3]) {
						if(((y1-1)<0)||((y1-1)>-1&&(i-1)>-1&&null!=gBoard[y1-1][i-1])) {
							stageTwo = true;
							safeX=x3+1;
							safeY=r-y3;
							stageTwo();
							return;
						}
						else
							continue;
					}
					else {
						if((i+1)<c) {
							if(null!=gBoard[y3][i+1])
								continue;
							else if(((y1-1)<0)||((y1-1)>-1&&(i+1)<c&&null!=gBoard[y1-1][i+1])) {
								stageTwo=true;
								safeX=i+1+1;
								safeY=r-y3;
								stageTwo();
								return;
							}
							else
								continue;
						}
					}
				}
				if(gBoard[y1][i].equals("1")&&gBoard[y2][i].equals("2")&&gBoard[y3][i].equals("1")) {
					if(null==gBoard[y1][x3] && null==gBoard[y3][x3]) {
						stageTwo = true;
						MyAI.gBoard[y1][x3]="B";
						MyAI.gBoard[y3][x3]="B";
						if(stageFour==true) {
							MyAI.gBoardEndGame[y1][x3]="B";
							MyAI.gBoardEndGame[y3][x3]="B";
						}
						bombay-=2;
						stageTwo();
					}
					else {
						if((i+1)<c) {
							if(null!=gBoard[y1][i+1]&&null!=gBoard[y3][i+1])
								continue;
							stageTwo=true;
							MyAI.gBoard[y1][i+1]="B";
							MyAI.gBoard[y3][i+1]="B";
							if(stageFour==true) {
								MyAI.gBoardEndGame[y1][i+1]="B";
								MyAI.gBoardEndGame[y3][i+1]="B";
							}
							bombay-=2;
							stageTwo();
						}
					}
				}
				if(!(y4>=r)&&!(null==gBoard[y4][i])&&gBoard[y1][i].equals("1") && gBoard[y2][i].equals("2")&&gBoard[y3][i].equals("2")&&("2".equals(gBoard[y4][i])||"1".equals(gBoard[y4][i]))) {
					if(null==gBoard[y2][x3] && null==gBoard[y3][x3]) {
						stageTwo = true;
						MyAI.gBoard[y2][x3]="B";
						MyAI.gBoard[y3][x3]="B";
						if(stageFour==true) {
							MyAI.gBoardEndGame[y2][x3]="B";
							MyAI.gBoardEndGame[y3][x3]="B";
						}
						bombay-=2;
						stageTwo();
					}
					else {
						if((i+1)<c) {
							if(null!=gBoard[y2][i+1]&&null!=gBoard[y3][i+1])
								continue;
							stageTwo=true;
							MyAI.gBoard[y2][i+1]="B";
							MyAI.gBoard[y3][i+1]="B";
							if(stageFour==true) {
								MyAI.gBoardEndGame[y2][i+1]="B";
								MyAI.gBoardEndGame[y3][i+1]="B";
							}
							bombay-=2;
							stageTwo();
						}
					}
				}
				if(gBoard[y1][i].equals("1")&&gBoard[y2][i].equals("2")&&gBoard[y3][i].equals("B")) {
					if(null==gBoard[y3][x3]) {
						stageTwo=true;
						safeX=x3+1;
						safeY=r-y3;
						stageTwo();
						return;
					}
					else {
						if((i+1)<c) {
							if(null!=gBoard[y3][i+1])
								continue;
							stageTwo=true;
							safeX=i+2;
							safeY=r-y3;
							stageTwo();
							return;
						}
					}
				}
			}
		//Up
		for(int i=0;i<gBoard[0].length;i++)
			for(int j=gBoard.length-1;j>=0;j--) {
				y1=j;
				y2=j-1;
				y3=j-2;
				y4=j-3;
				x3=i-1;
				if(y2<0||y3<0||x3<0||null==gBoard[y1][i]||null==gBoard[y2][i]||null==gBoard[y3][i])
					continue;
				if(gBoard[y1][i].equals("1") && gBoard[y2][i].equals("1")&&gBoard[y3][i].equals("1")&&(y4<0||(y4>=0&&null!=gBoard[y4][i]&&gBoard[y4][i].equals("1")))) {
					if(null==gBoard[y3][x3]) {
						if(((y1+1)>=r)||((y1+1)<r&&(i-1)>-1&&null!=gBoard[y1+1][i-1])) {
							stageTwo = true;
							safeX=x3+1;
							safeY=r-y3;
							stageTwo();
							return;
						}
						else
							continue;
					}
					else {
						if((i+1)<c) {
							if(null!=gBoard[y3][i+1])
								continue;
							else if(((y1+1)>=r)||((y1+1)<r&&(i+1)<r&&null!=gBoard[y1+1][i+1])) {
								stageTwo=true;
								safeX=i+1+1;
								safeY=r-y3;
								stageTwo();
								return;
							}
							else
								continue;
						}
					}
				}
				if(gBoard[y1][i].equals("1")&&gBoard[y2][i].equals("2")&&gBoard[y3][i].equals("1")) {
					if(null==gBoard[y1][x3] && null==gBoard[y3][x3]) {
						stageTwo = true;
						MyAI.gBoard[y1][x3]="B";
						MyAI.gBoard[y3][x3]="B";
						if(stageFour==true) {
							MyAI.gBoardEndGame[y1][x3]="B";
							MyAI.gBoardEndGame[y3][x3]="B";
						}
						bombay--;
						stageTwo();
					}
					else {
						if((i+1)<c) {
							if(null!=gBoard[y1][i+1]&&null!=gBoard[y3][i+1])
								continue;
							stageTwo=true;
							MyAI.gBoard[y1][i+1]="B";
							MyAI.gBoard[y3][i+1]="B";
							if(stageFour==true) {
								MyAI.gBoardEndGame[y1][i+1]="B";
								MyAI.gBoardEndGame[y3][i+1]="B";
							}
							bombay-=2;
							stageTwo();
						}
					}
				}
				if(!(y4<0)&&!(null==gBoard[y4][i])&&gBoard[y1][i].equals("1") && gBoard[y2][i].equals("2")&&gBoard[y3][i].equals("2")&&("2".equals(gBoard[y4][i])||"1".equals(gBoard[y4][i]))) {
					if(null==gBoard[y2][x3] && null==gBoard[y3][x3]) {
						stageTwo = true;
						MyAI.gBoard[y2][x3]="B";
						MyAI.gBoard[y3][x3]="B";
						if(stageFour==true) {
							MyAI.gBoardEndGame[y2][x3]="B";
							MyAI.gBoardEndGame[y3][x3]="B";
						}
						bombay--;
						stageTwo();
					}
					else {
						if((i+1)<c) {
							if(null!=gBoard[y2][i+1]&&null!=gBoard[y3][i+1])
								continue;
							stageTwo=true;
							MyAI.gBoard[y2][i+1]="B";
							MyAI.gBoard[y3][i+1]="B";
							if(stageFour==true) {
								MyAI.gBoardEndGame[y2][i+1]="B";
								MyAI.gBoardEndGame[y3][i+1]="B";
							}
							bombay-=2;
							stageTwo();
						}
					}
				}
				if(gBoard[y1][i].equals("1")&&gBoard[y2][i].equals("2")&&gBoard[y3][i].equals("B")) {
					if(null==gBoard[y3][x3]) {
						stageTwo=true;
						safeX=x3+1;
						safeY=r-y3;
						stageTwo();
						return;
					}
					else {
						if((i+1)<c) {
							if(null!=gBoard[y3][i+1])
								continue;
							stageTwo=true;
							safeX=i+2;
							safeY=r-y3;
							stageTwo();
							return;
						}
					}
				}
			}
	}

	private void checkNewCenter() {
		int a,b;
		a=r-centerY;
		b=centerX-1;
		if(null!=gBoard[a][b] && (!gBoard[a][b].equals("B")))
			shiftCenter();
		else return;
	}

	@SuppressWarnings("rawtypes")
	private void shiftCenter() {
		if(!stageOne) {
			Iterator<?> itah=zeroes.entrySet().iterator();
			while(itah.hasNext()) {
				Map.Entry pair=(Map.Entry)itah.next();
				if((int)pair.getValue()==0) {
					String temp[]=((String) pair.getKey()).split(",");
					centerX=Integer.valueOf(temp[0]);
					centerY=Integer.valueOf(temp[1]);
					zeroes.put((String) pair.getKey(), 1);
					flagCenter=1;
					break;
				}
				flagCenter=0;
			}
		}
		if(flagCenter==0) {
			stageOne = true;
			if(numMines==1) {
				detectOneBombCorners();
				checkNewCenter();
				openOneBombSurrounding();
			}
			else {
				stageTwo();
			}
		}
	}

	private void stageTwo() {
		int i,j,status;
		for(i=0;i<gBoard.length;i++)
			for(j=0;j<gBoard[0].length;j++) {
				if(null==gBoard[i][j])
					continue;
				switch(gBoard[i][j]) {
				case "0":status = getSurroundStatus(i,j);

				case "1":
				case "2":
				case "3":
				case "4":
				case "5":
				case "6":
				case "7":
				case "8":status = getSurroundStatus(i,j);
				if(status == 1) {
					i=0;
					j=-1;
					continue;
				}
				if(status == 2) {
					return;
				}
				default: continue;

				}
			}
	}

	private int getSurroundStatus(int x, int y) {
		int nullCount=0;
		int bombCount = 0;
		int status = 0;
		int value = Integer.parseInt(gBoard[x][y]);
		int key =0;
		HashMap <Integer,String> nulls= new HashMap<Integer, String>();
		for(int i=0;i<8;i++) {
			if((surroundCells[i][0]+x)<0 || (surroundCells[i][0]+x)>r-1 || (surroundCells[i][1]+y)<0 || (surroundCells[i][1]+y)>c-1) {
				continue;
			}
			if(null==gBoard[surroundCells[i][0]+x][surroundCells[i][1]+y]) {
				++nullCount;
				nulls.put(key++, (surroundCells[i][0]+x)+","+(surroundCells[i][1]+y));
			}
			else if(gBoard[surroundCells[i][0]+x][surroundCells[i][1]+y].equals("B"))
				++bombCount;
		}
		if(nullCount+bombCount == value) {
			for(int j=0;j<key;j++) {
				String babmoocords[] = nulls.get(j).split(",");
				gBoard[Integer.parseInt(babmoocords[0])][Integer.parseInt(babmoocords[1])]="B";
				bombay--;
				status = 1;
			}
		}
		if(bombCount == value) {
			for(int j=0;j<key;j++) {
				if(nulls.size()>0) {
					cords.put(cordCounter++,nulls.get(j));
					status = 2;
				}
				else
					status = 0;
			}
		}
		return status;
	}

	private void detectOneBombCorners() {
		int i,j;
		for(i=0;i<gBoard.length;i++)
			for(j=0;j<gBoard[0].length;j++) {
				if(null!=gBoard[i][j] && gBoard[i][j].equals("1")) {
					//Corner-1
					if(j+1<gBoard[0].length && i+1<gBoard.length && null!=gBoard[i][j+1] && null!=gBoard[i+1][j]) {
						if(gBoard[i][j+1].equals("1")&&gBoard[i+1][j].equals("1")&&null==gBoard[i+1][j+1]) {
							gBoard[i+1][j+1]="B";
							bombay--;
							surround=0;
							centerY=r-(i+1);
							centerX=(j+1)+1;
							return;
						}
					}
					//Corner-2
					if(j-1>=0 && i+1<gBoard.length && null!=gBoard[i][j-1] && null!=gBoard[i+1][j]) {
						if(gBoard[i][j-1].equals("1")&&gBoard[i+1][j].equals("1")&&null==gBoard[i+1][j-1]) {
							gBoard[i+1][j-1]="B";
							bombay--;
							surround=0;
							centerY=r-(i+1);
							centerX=(j-1)+1;
							return;
						}
					}
					//Corner-3
					if(j+1<gBoard[0].length && i-1>=0 && null!=gBoard[i][j+1] && null!=gBoard[i-1][j]) {
						if(gBoard[i][j+1].equals("1")&&gBoard[i-1][j].equals("1")&&null==gBoard[i-1][j+1]) {
							gBoard[i-1][j+1]="B";
							bombay--;
							surround=0;
							centerY=r-(i-1);
							centerX=(j+1)+1;
							return;
						}
					}
					//Corner-4
					if(j-1>=0 && i-1>=0 && null!=gBoard[i][j-1] && null!=gBoard[i-1][j]) {
						if(gBoard[i][j-1].equals("1")&&gBoard[i-1][j].equals("1")&&null==gBoard[i-1][j-1]) {
							gBoard[i-1][j-1]="B";
							bombay--;
							surround=0;
							centerY=r-(i-1);
							centerX=(j-1)+1;
							return;
						}
					}
				}

			}

	}

	private void openOneBombSurrounding() {
		if( surround>=8) {
			return;
		}
		else {
			do {
				if(surround>=8)
					break;
				tempX=centerX+surroundCells[surround][0];
				tempY=centerY+surroundCells[surround][1];
				row=r-tempY;
				col=tempX-1;
				surround++;
			}while((tempX<1||tempX>c)||(tempY<1||tempY>r)||(gBoard[row][col]!=null));
		}
	}

	private void openSurrounding() {
		if( surround>=8) {
			surround = 0;
			shiftCenter();
			if(flagCenter!=0)
				openSurrounding();
		}
		else {
			do {
				if(surround>=8)
					break;
				tempX=centerX+surroundCells[surround][0];
				tempY=centerY+surroundCells[surround][1];
				row=r-tempY;
				col=tempX-1;
				surround++;
			}while((tempX<1||tempX>c)||(tempY<1||tempY>r)||(gBoard[row][col]!=null));
			if(surround>=8) {
				if(row>-1 && row<r && col>-1 && col<c && gBoard[row][col]==null)
					return;
				openSurrounding();
			}
		}
	}
	// ################### Helper Functions Go Here (optional) ##################
	// ...
}
