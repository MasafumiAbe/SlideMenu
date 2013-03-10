package jp.fumin.slidemenu;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

/**
 * 
 * This activity has a view group that has three panes for left-side menu, <br/>
 * right-side menu and main view.<br/>
 * This class provides a layout like Facebook app.
 * 
 * @author abemasafumi
 *
 */
public class SlideMenuActivity extends FragmentActivity {

	/** Previous touch point */
	private Point prePoint;

	/** Layout for left-side menu. */
	private View leftMenuView;

	/** Layout for right-side menu. */
	private View rightMenuView;

	/** Layout for main view. */
	private View mainView;

	/** View for touch event. */
	private View touchView;

	/** Width percent of left-side. */
	private float leftPercent;

	/** Width percent of right-side. */
	private float rightPercent;

	/** slide animation */
	private TranslateAnimation slideAnim;

	/** flag for whether main view was slid. */
	private boolean isSlid;

	/** flag for whether main view is animating. */
	private boolean isAnimating;

	/** millisecond time of the slide animation */
	private static final int ANIMATION_TIME = 200;

	/** Default width percent of left menu. */
	public static final float DEFAULT_LEFT_WIDTH_PERCENT = 0.6f;

	/** Default width percent of right menu. */
	public static final float DEFAULT_RIGHT_WIDTH_PERCENT = 0.9f;
	
	/** Counter of touch move. This is used to check whether swipe is vertical. */
	private int moveCount;
	
	/** A flag to check can open left menu. */
	private boolean canOpenLeft;
	
	/** A flag to check can open right menu. */
	private boolean canOpenRight;

	/**
	 * Set the slide menu layout.<br/>
	 * So, DON'T SET CONETNT VIEW.
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.__slide_menu);

		init();
	}

	/**
	 * Resize menus in here because mainview.getWidth is 0
	 * on other life cycle methods.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (!hasFocus) {
			return;
		}

		if (leftPercent < 0) {
			setLeftPercent(DEFAULT_LEFT_WIDTH_PERCENT);
		} else {
			setLeftPercent(leftPercent);
		}

		if (rightPercent < 0) {
			setRightPercent(DEFAULT_RIGHT_WIDTH_PERCENT);
		} else {
			setRightPercent(rightPercent);
		}
	}

	/**
	 * Open the left menu.
	 */
	public final void openLeftMenu() {
		
		if (!canOpenLeft) {
			return;
		}
		
		int left = (int) (mainView.getWidth() * leftPercent);
		int endX = left - mainView.getLeft();
		isSlid = true;
		
		leftMenuView.bringToFront();
		mainView.bringToFront();
		touchView.bringToFront();
		
		showSlideAnimation(endX, left);
	}

	/**
	 * Open the right menu.
	 */
	public final void openRightMenu() {
		
		if (!canOpenRight) {
			return;
		}
		
		int left = (int) (-mainView.getWidth() * rightPercent);
		int endX = left - mainView.getLeft();
		isSlid = true;
		
		rightMenuView.bringToFront();
		mainView.bringToFront();
		touchView.bringToFront();
		showSlideAnimation(endX, left);
	}

	/**
	 * Initialize variables.
	 */
	private void init() {
		prePoint = new Point();
		leftPercent = -1.0f;
		rightPercent = -1.0f;
		setCanOpenLeft(true);
		setCanOpenRight(true);
		initViews();
	}
	/**
	 * set left menu fragment
	 * 
	 * @param fragment fragment object that is set.
	 */
	protected final void setLeftMenuFragment(Fragment fragment) {
		setFragment(R.id.__slide_left_menu_contents, fragment);
	}

	/**
	 * set right menu fragment
	 * 
	 * @param fragment fragment object that is set.
	 */
	protected final void setRightMenuFragment(Fragment fragment) {
		setFragment(R.id.__slide_right_menu_contents, fragment);
	}

	/**
	 * set main fragment
	 * 
	 * @param fragment fragment object that is set.
	 */
	protected final void setMainFragment(Fragment fragment) {
		setFragment(R.id.__slide_main_contents, fragment);
	}

	/**
	 * replace the menu fragment
	 * 
	 * @param id resource id of left or right menu
	 * @param fragment Fragment object set.
	 */
	private void setFragment(int id, Fragment fragment) {
		if (fragment == null) {
			throw new IllegalArgumentException("fragment can not be null.");
		}
		getSupportFragmentManager().beginTransaction()
		.replace(id, fragment).commit();
	}

	/**
	 * Initialize menu views.
	 */
	private void initViews() {
		leftMenuView  = (View) findViewById(R.id.__slide_left_menu);
		rightMenuView = (View) findViewById(R.id.__slide_right_menu);
		mainView      = (View) findViewById(R.id.__slide_main);
		touchView     = (View) findViewById(R.id.__slide_touch);
		touchView.setOnTouchListener(new MainViewTouchListener());
	}
	
	/**
	 * check whether can open left menu.
	 *
	 * @return true if can open left menu, false otherwise.
	 */
	public boolean canOpenLeft() {
		return canOpenLeft;
	}

	/**
	 * set can open left menu.
	 *
	 * @param canOpenLeft If true, you can open left menu.
	 */
	public void setCanOpenLeft(boolean canOpenLeft) {
		this.canOpenLeft = canOpenLeft;
	}

	/**
	 * check whether can open right menu.
	 *
	 * @return true if can open right menu, false otherwise.
	 */
	public boolean canOpenRight() {
		return canOpenRight;
	}

	/**
	 * set can open right menu.
	 *
	 * @param canOpenRight If true, you can open right menu.
	 */
	public void setCanOpenRight(boolean canOpenRight) {
		this.canOpenRight = canOpenRight;
	}

	/**
	 * get percent of left menu width.
	 *
	 * @return the leftPercent
	 */
	public final float getLeftPercent() {
		return leftPercent;
	}

	/**
	 * set percent of left menu width and resize left menu view.
	 *
	 * @param leftPercent the leftPercent to set
	 */
	public final void setLeftPercent(float leftPercent) {
		this.leftPercent = leftPercent;
		int width = (int) (mainView.getWidth() * leftPercent);
		int height = mainView.getHeight();
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
		leftMenuView.setLayoutParams(params);
	}

	/**
	 * get percent of right menu width.
	 *
	 * @return the rightPercent
	 */
	public final float getRightPercent() {
		return rightPercent;
	}

	/**
	 * set percent of right menu width and resize right menu view.
	 *
	 * @param rightPercent the rightPercent to set
	 */
	public final void setRightPercent(float rightPercent) {
		this.rightPercent = rightPercent;
		int width = (int) (mainView.getWidth() * rightPercent);
		int height = mainView.getHeight();
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
		// reset layout gravity because layout gravity of right menu
		// will be left when resize.
		params.gravity = Gravity.RIGHT;
		rightMenuView.setLayoutParams(params);
	}

	private void showSlideAnimation(float endX, final int toLeft) {

		slideAnim = new TranslateAnimation(0, endX, 0, 0);
		slideAnim.setDuration(ANIMATION_TIME);
		slideAnim.setFillAfter(false);
		touchView.startAnimation(slideAnim);

		slideAnim = new TranslateAnimation(0, endX, 0, 0);
		slideAnim.setDuration(ANIMATION_TIME);
		slideAnim.setFillAfter(false);
		slideAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				isAnimating = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mainView.layout(toLeft, mainView.getTop(),
						toLeft + mainView.getWidth(), mainView.getTop()
						+ mainView.getHeight());
				touchView.layout(toLeft, mainView.getTop(),
						toLeft + mainView.getWidth(), mainView.getTop()
						+ mainView.getHeight());
				mainView.setAnimation(null);
				touchView.setAnimation(null);
				isAnimating = false;
			}
		});
		mainView.startAnimation(slideAnim);
	}

	/**
	 * Listener class for touch event of main view.
	 * 
	 * @author abemasafumi
	 */
	private class MainViewTouchListener implements View.OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			if (v.getLeft() != 0) {
				isSlid = true;
				moveCount++;
			}

			int tx = (int) event.getRawX();
			int ty = (int) event.getRawY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (slideAnim != null && isAnimating) {
					mainView.setAnimation(null);
					touchView.setAnimation(null);
					isAnimating = false;
				}
				moveCount = 0;
				break;
			case MotionEvent.ACTION_MOVE:
			{
				
				int dx = Math.abs(tx - prePoint.x);
				int dy = Math.abs(ty - prePoint.y);
				
				if (moveCount == 0) {
					isSlid = dx > dy;
					moveCount++;
				}
				
				if (!isSlid) {
					break;
				}
				
				int left = v.getLeft() + (tx - prePoint.x);
				int top = v.getTop();

				if (left > 0) {
					leftMenuView.bringToFront();

					if (!canOpenLeft()) {
						left = 0;
					} else if (left > v.getWidth() * leftPercent) {
						left = (int) (v.getWidth() * leftPercent);
					}
					
				} else if (left < 0) {
					rightMenuView.bringToFront();

					if (!canOpenRight()) {
						left = 0;
					} else if (left < -v.getWidth() * rightPercent) {
						left = (int) (-v.getWidth() * rightPercent);
					}
				}

				mainView.bringToFront();
				touchView.bringToFront();

				v.layout(left, top, left + v.getWidth(), top + v.getHeight());
				mainView.layout(left, top, left + v.getWidth(), top + v.getHeight());
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				int left = 0;
				float endX = 0.0f;

				if (mainView.getLeft() > 0) {
					int center = (int) (mainView.getWidth() * leftPercent) / 2;
					if (mainView.getLeft() > center) {
						left = (int) (mainView.getWidth() * leftPercent);
						endX = left - mainView.getLeft();
					} else {
						left = 0;
						endX = -mainView.getLeft();
					}
				} else if (mainView.getLeft() < 0) {
					int center = (int) (-mainView.getWidth() * rightPercent) / 2;
					if (mainView.getLeft() < center) {
						left = (int) (-mainView.getWidth() * rightPercent);
						endX = left - mainView.getLeft();
					} else {
						left = 0;
						endX = -mainView.getLeft();
					}
				}

				showSlideAnimation(endX, left);
				
				if (isSlid) {
					event.setAction(MotionEvent.ACTION_CANCEL);					
				}
				
				isSlid = false;
				break;
			default:
				break;
			}

			prePoint.set(tx, ty);
			
			if (!isSlid) {
				mainView.dispatchTouchEvent(event);
			} else if (moveCount == 1) {
				// create new event to notify cancel event to children of main view.
				// this process is called only once because cancel event commonly
				// happens only one time.
				MotionEvent cancelEvent = MotionEvent.obtain(event);
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
				mainView.dispatchTouchEvent(cancelEvent);
				cancelEvent.recycle();
			}
			
			return true;
		}
	}
}
