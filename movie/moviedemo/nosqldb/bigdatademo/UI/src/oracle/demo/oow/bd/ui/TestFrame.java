package oracle.demo.oow.bd.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import javax.swing.SwingWorker;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.dao.GenreDAO;
import oracle.demo.oow.bd.to.GenreTO;
import oracle.demo.oow.bd.to.MovieTO;

public class TestFrame extends JFrame {


    private JToolBar jToolBar1 = new JToolBar();
    private JToolBar jToolBar2 = new JToolBar();
    private JToolBar jToolBar3 = new JToolBar();
    private JPanel topPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();
    private JLabel photographLabel = new JLabel();

    private GenreDAO genreDAO = new GenreDAO();
    private List<MovieTO> movieTOList1 = null;
    private List<MovieTO> movieTOList2 = null;
    private List<MovieTO> movieTOList3 = null;
    private int movieCount = 10;

    public TestFrame() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {


        this.getContentPane().setLayout(new BorderLayout());
        this.setSize(new Dimension(721, 532));
        this.setExtendedState(MAXIMIZED_BOTH);

        //Add JPanel to the Frame
        this.getContentPane().add(topPanel, BorderLayout.NORTH);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        //Add JToolBar(s) to the bottomPane
        bottomPanel.add(jToolBar1, null);
        bottomPanel.add(jToolBar2, null);
        bottomPanel.add(jToolBar3, null);

        // A label for displaying the pictures
        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        //Add it to topPanel
        topPanel.add(photographLabel);

        // start the image loading SwingWorker in a background thread
        loadimages.execute();

    } //jbInit

    /**
     * SwingWorker class that loads the images a background thread and calls publish
     * when a new one is ready to be displayed.
     *
     * We use Void as the first SwingWorker param as we do not need to return
     * anything from doInBackground().
     */
    private SwingWorker<Void, ThumbnailAction> loadimages =
        new SwingWorker<Void, ThumbnailAction>() {

        /**
         * Creates full size and thumbnail versions of the target image files.
         */
        @Override
        protected Void doInBackground() throws Exception {


            //download 8 movies per genre
            movieTOList1 = genreDAO.getMoviesByGenre(35, movieCount);
            movieTOList2 = genreDAO.getMoviesByGenre(16, movieCount);
            movieTOList3 = genreDAO.getMoviesByGenre(12, movieCount);
            
            
          List<GenreTO> genres =  genreDAO.getGenres();
          System.out.println(genres.size());
            
            MovieTO movieTO = null;
            String title = null;
            String imgUrl = null;
            JButton jButton = null;
            ThumbnailAction thumbAction;
            ImageIcon thumbnailIcon = null;


            for (int i = 0; i < movieCount; i++) {
                ImageIcon icon = null;
                /**
                 * First movie list
                 */
                movieTO = movieTOList1.get(i);
                title = movieTO.getTitle();

                imgUrl = Constant.TMDb_IMG_URL + movieTO.getPosterPath();
              
                icon = createImageIcon(imgUrl, title);
                jButton = new JButton(icon);
                jToolBar1.add(jButton);
                thumbAction = new ThumbnailAction(icon, icon, title);
                publish(thumbAction);

                /**
                 * Second movie list
                 */
                movieTO = movieTOList2.get(i);
                title = movieTO.getTitle();

                imgUrl = Constant.TMDb_IMG_URL + movieTO.getPosterPath();
                icon = createImageIcon(imgUrl, title);
                jButton = new JButton(icon);
                jToolBar2.add(jButton);
                thumbAction = new ThumbnailAction(icon, icon, title);
                publish(thumbAction);

                /**
                 * Third movie list
                 */
                movieTO = movieTOList3.get(i);
                title = movieTO.getTitle();

                imgUrl = Constant.TMDb_IMG_URL + movieTO.getPosterPath();
                icon = createImageIcon(imgUrl, title);
              
                jButton = new JButton(icon);
                jToolBar3.add(jButton);
                thumbAction = new ThumbnailAction(icon, icon, title);
                publish(thumbAction);

            } //EOF for


            // unfortunately we must return something, and only null is valid to
            // return when the return type is void.
            return null;
        }
    };


    /**
     * Creates an ImageIcon if the path is valid.
     * @param path - resource path
     * @param description - of the file
     */
    protected ImageIcon createImageIcon(String path, String description) {
        URL imgURL = null;
        ImageIcon icon = null;

        try {
            imgURL = new URL(path);
            icon = new ImageIcon(imgURL, description);
            icon.setImage(getScaledImage(icon.getImage(), 100, 150));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return icon;


    }

    /**
     * Resizes an image using a Graphics2D object backed by a BufferedImage.
     * @param srcImg - source image to scale
     * @param w - desired width
     * @param h - desired height
     * @return - the new resized image
     */
    private Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg =
            new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }

    /**
     * Action class that shows the image specified in it's constructor.
     */
    private class ThumbnailAction extends AbstractAction {


        /**
         *The icon if the full image we want to display.
         */
        private Icon displayPhoto;

        /**
         * @param photo - The full size photo to show in the button.
         * @param thumb - The thumbnail to show in the button.
         * @param desc - The descriptioon of the icon.
         */
        public ThumbnailAction(Icon photo, Icon thumb, String desc) {
            displayPhoto = photo;

            // The short description becomes the tooltip of a button.
            putValue(SHORT_DESCRIPTION, desc);

            // The LARGE_ICON_KEY is the key for setting the
            // icon when an Action is applied to a button.
            putValue(LARGE_ICON_KEY, thumb);
        }

        /**
         * Shows the full image in the main area and sets the application title.
         */
        public void actionPerformed(ActionEvent e) {
            System.out.println("Action Perform");
            photographLabel.setIcon(displayPhoto);
            setTitle("Icon Demo: " + getValue(SHORT_DESCRIPTION).toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TestFrame app = new TestFrame();
                    app.setVisible(true);
                }
            });
    }

}
