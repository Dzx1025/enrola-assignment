package com.getenrola.aidemo.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;


class ProductTools {

    public record ProductInfoRequest(String productName) {
    }

    public record ProductInfoResponse(String title,
                                      @ToolParam(description = "Price by AUD") String price,
                                      @ToolParam(description = "Color of product") String color,
                                      @ToolParam(description = "Ink") String ink,
                                      String Case,
                                      String uniqueness,
                                      @ToolParam(description = "Product Offering") String includes,
                                      @ToolParam(description = "Purchase link") String purchaseLink) {
    }

    @Tool(description = "Query the product's price, color, and purchase link based on the product name.")
    public ProductInfoResponse getProductDetails(ProductInfoRequest request) {
        // Mock implementation - in real scenario, fetch from database or external API
        return new ProductInfoResponse(
                "One-of-a-Kind Luxury Pen",
                "$5,000",
                "Black",
                "Premium black ink (Swiss-engineered, 50-year reserve)",
                "Aerospace-grade titanium with ethically-sourced diamonds",
                "ONLY ONE EXISTS - once sold, gone forever",
                "Lifetime servicing, display case, certificate of authenticity",
                "https://bit.ly/fakepen/"
        );
    }

}
