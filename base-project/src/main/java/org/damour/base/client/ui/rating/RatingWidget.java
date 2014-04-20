package org.damour.base.client.ui.rating;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.UserRating;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class RatingWidget extends VerticalPanel {

  Label statsLabel = null;
  boolean showStatsLabel = true;
  HorizontalPanel starPanel = new HorizontalPanel();
  PermissibleObject permissibleObject;
  UserRating userRating;

  Image star1 = new Image();
  Image star2 = new Image();
  Image star3 = new Image();
  Image star4 = new Image();
  Image star5 = new Image();

  private boolean isSubmitting = false;
  private boolean interactive = true;

  ClickHandler starClickHandler = new ClickHandler() {

    public void onClick(ClickEvent event) {
      if (interactive && userRating == null) {
        // do vote
        int vote = 0;
        if (event.getSource() == star1) {
          vote = 1;
        } else if (event.getSource() == star2) {
          vote = 2;
        } else if (event.getSource() == star3) {
          vote = 3;
        } else if (event.getSource() == star4) {
          vote = 4;
        } else if (event.getSource() == star5) {
          vote = 5;
        }
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "wait");
        setUserRating(permissibleObject, vote);
      }
    }
  };

  MouseOverHandler starOverHandler = new MouseOverHandler() {

    public void onMouseOver(MouseOverEvent event) {
      if (isSubmitting) {
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "wait");
        return;
      }
      if (interactive && userRating == null) {
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "hand");
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "pointer");
        starMoused((Widget) event.getSource());
      } else {
        DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "default");
      }
    }
  };

  MouseOutHandler starOutHandler = new MouseOutHandler() {

    public void onMouseOut(MouseOutEvent event) {
      DOM.setStyleAttribute(((Widget) event.getSource()).getElement(), "cursor", "default");
      setStars();
    }
  };

  MouseUpHandler starUpHandler = new MouseUpHandler() {

    public void onMouseUp(MouseUpEvent event) {
      setStars();
    }
  };

  public RatingWidget(PermissibleObject permissibleObject, UserRating fileRating, boolean interactive, boolean fetchOnLoad, boolean showStatsLabel) {
    this.showStatsLabel = showStatsLabel;
    this.permissibleObject = permissibleObject;
    this.userRating = fileRating;
    this.interactive = interactive;
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    setupStar(star1);
    setupStar(star2);
    setupStar(star3);
    setupStar(star4);
    setupStar(star5);

    add(starPanel);

    statsLabel = new Label(BaseApplication.getMessages().getString("ratingStatsLabel", "{0} rating from {1} users",
        NumberFormat.getFormat("0.0").format(permissibleObject.getAverageRating()), "" + permissibleObject.getNumRatingVotes()), false);
    DOM.setStyleAttribute(statsLabel.getElement(), "fontSize", "8pt");
    if (showStatsLabel) {
      add(statsLabel);
    }
    if (interactive && fileRating == null && permissibleObject.getNumRatingVotes() > 0 && fetchOnLoad) {
      getUserRating(permissibleObject);
    } else {
      setStars();
    }
  }

  public void starMoused(Widget sender) {
    if (sender == star1) {
      star1.setResource(BaseImageBundle.images.starHover());
      star1.setTitle(BaseApplication.getMessages().getString("ratingAwful", "Awful"));
      star2.setResource(BaseImageBundle.images.starEmpty());
      star3.setResource(BaseImageBundle.images.starEmpty());
      star4.setResource(BaseImageBundle.images.starEmpty());
      star5.setResource(BaseImageBundle.images.starEmpty());
    } else if (sender == star2) {
      star1.setResource(BaseImageBundle.images.starHover());
      star2.setResource(BaseImageBundle.images.starHover());
      star2.setTitle(BaseApplication.getMessages().getString("ratingPoor", "Poor"));
      star3.setResource(BaseImageBundle.images.starEmpty());
      star4.setResource(BaseImageBundle.images.starEmpty());
      star5.setResource(BaseImageBundle.images.starEmpty());
    } else if (sender == star3) {
      star1.setResource(BaseImageBundle.images.starHover());
      star2.setResource(BaseImageBundle.images.starHover());
      star3.setResource(BaseImageBundle.images.starHover());
      star3.setTitle(BaseApplication.getMessages().getString("ratingNotBad", "Not Bad"));
      star4.setResource(BaseImageBundle.images.starEmpty());
      star5.setResource(BaseImageBundle.images.starEmpty());
    } else if (sender == star4) {
      star1.setResource(BaseImageBundle.images.starHover());
      star2.setResource(BaseImageBundle.images.starHover());
      star3.setResource(BaseImageBundle.images.starHover());
      star4.setResource(BaseImageBundle.images.starHover());
      star4.setTitle(BaseApplication.getMessages().getString("ratingGood", "Good"));
      star5.setResource(BaseImageBundle.images.starEmpty());
    } else if (sender == star5) {
      star1.setResource(BaseImageBundle.images.starHover());
      star2.setResource(BaseImageBundle.images.starHover());
      star3.setResource(BaseImageBundle.images.starHover());
      star4.setResource(BaseImageBundle.images.starHover());
      star5.setResource(BaseImageBundle.images.starHover());
      star5.setTitle(BaseApplication.getMessages().getString("ratingGreat", "Great"));
    }
  }

  private Image setupStar(Image star) {
    star.addClickHandler(starClickHandler);
    star.addMouseOverHandler(starOverHandler);
    star.addMouseOutHandler(starOutHandler);
    star.addMouseUpHandler(starUpHandler);
    DOM.setStyleAttribute(star.getElement(), "margin", "0px");
    DOM.setStyleAttribute(star.getElement(), "padding", "0px");
    starPanel.add(star);
    star.setTitle("");
    return star;
  }

  public void setStars() {
    String statText = BaseApplication.getMessages().getString("ratingStatsLabel", "{0} rating from {1} users",
        NumberFormat.getFormat("0.0").format(permissibleObject.getAverageRating()), "" + permissibleObject.getNumRatingVotes());
    statsLabel.setText(statText);
    if (permissibleObject.getNumRatingVotes() == 0) {
      star1.setResource(BaseImageBundle.images.starNoVotes());
      star2.setResource(BaseImageBundle.images.starNoVotes());
      star3.setResource(BaseImageBundle.images.starNoVotes());
      star4.setResource(BaseImageBundle.images.starNoVotes());
      star5.setResource(BaseImageBundle.images.starNoVotes());
    } else {
      float rating = permissibleObject.getAverageRating();
      if (rating < .25) {
        // 0
        star1.setResource(BaseImageBundle.images.starNoVotes());
        star2.setResource(BaseImageBundle.images.starNoVotes());
        star3.setResource(BaseImageBundle.images.starNoVotes());
        star4.setResource(BaseImageBundle.images.starNoVotes());
        star5.setResource(BaseImageBundle.images.starNoVotes());
      } else if (rating >= .25 && rating < .75) {
        // .5
        star1.setResource(BaseImageBundle.images.starHalf());
        star2.setResource(BaseImageBundle.images.starNoVotes());
        star3.setResource(BaseImageBundle.images.starNoVotes());
        star4.setResource(BaseImageBundle.images.starNoVotes());
        star5.setResource(BaseImageBundle.images.starNoVotes());
      } else if (rating >= .75 && rating < 1.25) {
        // 1.0
        star1.setResource(BaseImageBundle.images.starFull());
        star2.setResource(BaseImageBundle.images.starNoVotes());
        star3.setResource(BaseImageBundle.images.starNoVotes());
        star4.setResource(BaseImageBundle.images.starNoVotes());
        star5.setResource(BaseImageBundle.images.starNoVotes());
      } else if (rating >= 1.25 && rating < 1.75) {
        // 1.5
        star1.setResource(BaseImageBundle.images.starFull());
        star2.setResource(BaseImageBundle.images.starHalf());
        star3.setResource(BaseImageBundle.images.starNoVotes());
        star4.setResource(BaseImageBundle.images.starNoVotes());
        star5.setResource(BaseImageBundle.images.starNoVotes());
      } else if (rating >= 1.75 && rating < 2.25) {
        // 2.0
        star1.setResource(BaseImageBundle.images.starFull());
        star2.setResource(BaseImageBundle.images.starFull());
        star3.setResource(BaseImageBundle.images.starNoVotes());
        star4.setResource(BaseImageBundle.images.starNoVotes());
        star5.setResource(BaseImageBundle.images.starNoVotes());
      } else if (rating >= 2.25 && rating < 2.75) {
        // 2.5
        star1.setResource(BaseImageBundle.images.starFull());
        star2.setResource(BaseImageBundle.images.starFull());
        star3.setResource(BaseImageBundle.images.starHalf());
        star4.setResource(BaseImageBundle.images.starNoVotes());
        star5.setResource(BaseImageBundle.images.starNoVotes());
      } else if (rating >= 2.75 && rating < 3.25) {
        // 3.0
        star1.setResource(BaseImageBundle.images.starFull());
        star2.setResource(BaseImageBundle.images.starFull());
        star3.setResource(BaseImageBundle.images.starFull());
        star4.setResource(BaseImageBundle.images.starNoVotes());
        star5.setResource(BaseImageBundle.images.starNoVotes());
      } else if (rating >= 3.25 && rating < 3.75) {
        // 3.5
        star1.setResource(BaseImageBundle.images.starFull());
        star2.setResource(BaseImageBundle.images.starFull());
        star3.setResource(BaseImageBundle.images.starFull());
        star4.setResource(BaseImageBundle.images.starHalf());
        star5.setResource(BaseImageBundle.images.starNoVotes());
      } else if (rating >= 3.75 && rating < 4.25) {
        // 4.0
        star1.setResource(BaseImageBundle.images.starFull());
        star2.setResource(BaseImageBundle.images.starFull());
        star3.setResource(BaseImageBundle.images.starFull());
        star4.setResource(BaseImageBundle.images.starFull());
        star5.setResource(BaseImageBundle.images.starNoVotes());
      } else if (rating >= 4.25 && rating < 4.75) {
        // 4.5
        star1.setResource(BaseImageBundle.images.starFull());
        star2.setResource(BaseImageBundle.images.starFull());
        star3.setResource(BaseImageBundle.images.starFull());
        star4.setResource(BaseImageBundle.images.starFull());
        star5.setResource(BaseImageBundle.images.starHalf());
      } else if (rating >= 4.75) {
        // 5
        star1.setResource(BaseImageBundle.images.starFull());
        star2.setResource(BaseImageBundle.images.starFull());
        star3.setResource(BaseImageBundle.images.starFull());
        star4.setResource(BaseImageBundle.images.starFull());
        star5.setResource(BaseImageBundle.images.starFull());
      }
    }

    if (showStatsLabel && userRating != null) {
      String title = BaseApplication.getMessages().getString("ratingAlready", "Content Rating (You have already voted)");
      star1.setTitle(title);
      star2.setTitle(title);
      star3.setTitle(title);
      star4.setTitle(title);
      star5.setTitle(title);
    } else {
      star1.setTitle(statText);
      star2.setTitle(statText);
      star3.setTitle(statText);
      star4.setTitle(statText);
      star5.setTitle(statText);
    }

    if (isSubmitting) {
      DOM.setStyleAttribute(star1.getElement(), "cursor", "wait");
      DOM.setStyleAttribute(star2.getElement(), "cursor", "wait");
      DOM.setStyleAttribute(star3.getElement(), "cursor", "wait");
      DOM.setStyleAttribute(star4.getElement(), "cursor", "wait");
      DOM.setStyleAttribute(star5.getElement(), "cursor", "wait");
    } else {
      DOM.setStyleAttribute(star1.getElement(), "cursor", "default");
      DOM.setStyleAttribute(star2.getElement(), "cursor", "default");
      DOM.setStyleAttribute(star3.getElement(), "cursor", "default");
      DOM.setStyleAttribute(star4.getElement(), "cursor", "default");
      DOM.setStyleAttribute(star5.getElement(), "cursor", "default");
    }

  }

  public UserRating getUserRating() {
    return userRating;
  }

  public void setUserRating(UserRating userRating) {
    this.userRating = userRating;
  }

  public void setUserRating(final PermissibleObject permissibleObject, int rating) {
    if (isSubmitting) {
      return;
    }
    isSubmitting = true;
    MethodCallback<UserRating> callback = new MethodCallback<UserRating>() {

      public void onFailure(Method method, Throwable exception) {
        isSubmitting = false;
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), exception.getMessage(), false, true, true);
        dialog.center();
      }

      public void onSuccess(Method method, UserRating userFileRating) {
        isSubmitting = false;
        if (userFileRating != null) {
          RatingWidget.this.userRating = userFileRating;
          userFileRating.getPermissibleObject().mergeInto(permissibleObject);
        }
        setStars();
      }
    };
    ResourceCache.getRatingResource().setUserRating(permissibleObject.getId(), rating, callback);
  }

  public void getUserRating(final PermissibleObject permissibleObject) {
    MethodCallback<UserRating> callback = new MethodCallback<UserRating>() {

      public void onFailure(Method method, Throwable exception) {
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), exception.getMessage(), false, true, true);
        dialog.center();
        clear();
      }

      public void onSuccess(Method method, UserRating userFileRating) {
        if (userFileRating != null) {
          RatingWidget.this.userRating = userFileRating;
          userFileRating.getPermissibleObject().mergeInto(permissibleObject);
        }
        setStars();
      }
    };
    ResourceCache.getRatingResource().getUserRating(permissibleObject.getId(), callback);
  }
}
