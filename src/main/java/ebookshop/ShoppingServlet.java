package ebookshop;

import java.util.Vector;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;

public class ShoppingServlet extends HttpServlet {
    
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        doPost(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        @SuppressWarnings("unchecked")
        Vector<Book> shoplist = (Vector<Book>) session.getAttribute("ebookshop.cart");
        String do_this = req.getParameter("do_this");

        // Initialize the list of books if it's the first request
        if (do_this == null) {
            Vector<String> blist = new Vector<>();
            blist.add("Learn HTML5 and JavaScript for iOS. Scott Preston $39.99");
            blist.add("Java 7 for Absolute Beginners. Jay Bryant $39.99");
            blist.add("Beginning Android 4. Livingston $39.99");
            blist.add("Pro Spatial with SQL Server 2012. Alastair Aitchison $59.99");
            blist.add("Beginning Database Design. Clare Churcher $34.99");

            session.setAttribute("ebookshop.list", blist);
            ServletContext sc = req.getSession().getServletContext();
            RequestDispatcher rd = sc.getRequestDispatcher("/");
            rd.forward(req, res);
        } else {
            // If not first request, handle shopping cart actions
            if (do_this.equals("checkout")) {
                float dollars = 0;
                int books = 0;

                if (shoplist != null) {
                    for (Book aBook : shoplist) {
                        dollars += aBook.getPrice() * aBook.getQuantity();
                        books += aBook.getQuantity();
                    }
                }

                req.setAttribute("dollars", String.valueOf(dollars));
                req.setAttribute("books", String.valueOf(books));
                ServletContext sc = req.getSession().getServletContext();
                RequestDispatcher rd = sc.getRequestDispatcher("/Checkout.jsp");
                rd.forward(req, res);
            } else {
                // Manipulate shopping cart
                if (shoplist == null) {
                    shoplist = new Vector<>();
                }

                if (do_this.equals("remove")) {
                    String pos = req.getParameter("position");
                    shoplist.remove(Integer.parseInt(pos));
                } else if (do_this.equals("add")) {
                    boolean found = false;
                    Book aBook = getBook(req);

                    // Update quantity if book already exists
                    for (int i = 0; i < shoplist.size() && !found; i++) {
                        Book b = shoplist.elementAt(i);
                        if (b.getTitle().equals(aBook.getTitle())) {
                            b.setQuantity(b.getQuantity() + aBook.getQuantity());
                            shoplist.setElementAt(b, i);
                            found = true;
                        }
                    }
                    if (!found) {
                        shoplist.add(aBook);
                    }
                }

                // Save updated shopping cart and return to home
                session.setAttribute("ebookshop.cart", shoplist);
                ServletContext sc = getServletContext();
                RequestDispatcher rd = sc.getRequestDispatcher("/");
                rd.forward(req, res);
            }
        }
    }

    private Book getBook(HttpServletRequest req) {
        String myBook = req.getParameter("book");
        int n = myBook.lastIndexOf('$'); // Ensure we find the last '$' character
        String title = myBook.substring(0, n).trim();
        String price = myBook.substring(n + 1).trim();
        String qty = req.getParameter("qty");

        return new Book(title, Float.parseFloat(price), Integer.parseInt(qty));
    }
}
