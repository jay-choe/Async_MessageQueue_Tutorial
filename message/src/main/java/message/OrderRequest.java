package message;

public class OrderRequest {
  private int productId;
  private int stock;

  public int getProductId() {
      return this.productId;
    }

    public int getStock() {
      return this.stock;
    }

    public OrderRequest( final int productId,  final int stock) {
      this .productId = productId;
      this .stock = stock;
    }

    public OrderRequest() {

  }
}
