package org.damour.base.client.objects;


public class Photo extends File {

  public PhotoThumbnail slideshowImage;
  public PhotoThumbnail previewImage;
  public PhotoThumbnail thumbnailImage;
  public int height;
  public int width;

  public Photo() {
  }

  public PhotoThumbnail getSlideshowImage() {
    return slideshowImage;
  }

  public void setSlideshowImage(PhotoThumbnail slideshowImage) {
    this.slideshowImage = slideshowImage;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public PhotoThumbnail getPreviewImage() {
    return previewImage;
  }

  public PhotoThumbnail getThumbnailImage() {
    return thumbnailImage;
  }

  public void setThumbnailImage(PhotoThumbnail thumbnailImage) {
    this.thumbnailImage = thumbnailImage;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public void setPreviewImage(PhotoThumbnail previewImage) {
    this.previewImage = previewImage;
  }

}
