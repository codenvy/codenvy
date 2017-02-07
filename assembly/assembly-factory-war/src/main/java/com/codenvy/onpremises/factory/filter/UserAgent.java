/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.onpremises.factory.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vitaliy Guliy
 */
public class UserAgent {

    private static final Logger LOG = LoggerFactory.getLogger(UserAgent.class);

    public class Product {

        String name;

        String version;

        List<String> comments = new ArrayList<>();

        public Product(String product, String comments) {
            int slash = product.indexOf("/");
            if (slash > 0) {
                name = product.substring(0, slash);
                version = product.substring(slash + 1);
            } else {
                name = product;
                version = "";
            }

            if (comments != null && !comments.isEmpty()) {
                String[] parts = comments.substring(1, comments.length() - 1).split("; ");
                Collections.addAll(this.comments, parts);
            }
        }

        public void dump() {
            System.out.println("--- PRODUCT ---");

            System.out.println("NAME " + name);
            System.out.println("VERSION " + version);

            for (String comment : comments) {
                System.out.println("   [" + comment + "]");
            }
        }

    }


    private List<Product> products = new ArrayList<>();

    public UserAgent(String header) {
        parse(header);
    }

    public void parse(String header) {
        try {
            int index = 0;
            String product = "";
            String comments = "";

            while (index < header.length() - 1) {
                char ch = header.charAt(index);

                if (ch == ' ') {
                    index++;
                    continue;
                }

                if (ch == '(') {
                    // read comment
                    comments = "";

                    while (true) {
                        comments += header.charAt(index);
                        if (header.charAt(index) == ')') {
                            products.add(new Product(product, comments));
                            product = "";
                            index++;
                            break;
                        }

                        index++;

                        if (index == header.length()) {
                            throw new Exception("Product comments section must ends with ')' character");
                        }
                    }

                } else {
                    if (!product.isEmpty()) {
                        products.add(new Product(product, comments));
                        product = "";
                    }

                    while (true) {
                        product += header.charAt(index);
                        index++;

                        if (index == header.length() || header.charAt(index) == ' ') {
                            break;
                        }
                    }
                }

            }

            if (!product.isEmpty()) {
                products.add(new Product(product, comments));
            }

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    public boolean hasProduct(String product) {
        if (product == null || product.trim().isEmpty()) {
            return false;
        }

        for (Product p : products) {
            if (p.name != null && !p.name.isEmpty() && p.name.equals(product.trim())) {
                return true;
            }
        }

        return false;
    }

    public boolean hasProductPart(String productPart) {
        if (productPart == null || productPart.trim().isEmpty()) {
            return false;
        }

        for (Product p : products) {
            if (p.name != null && !p.name.isEmpty() && p.name.startsWith(productPart)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }

        for (Product p : products) {
            for (String c : p.comments) {
                if (c.equals(comment)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasCommentPart(String commentPart) {
        if (commentPart != null && !commentPart.trim().isEmpty()) {
            for (Product p : products) {
                for (String c : p.comments) {
                    if (c.startsWith(commentPart)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
