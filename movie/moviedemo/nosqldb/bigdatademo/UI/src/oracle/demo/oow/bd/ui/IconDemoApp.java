package oracle.demo.oow.bd.ui;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

import java.awt.image.BufferedImage;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;

import javax.swing.AbstractAction;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.dao.GenreDAO;
import oracle.demo.oow.bd.to.MovieTO;

public class IconDemoApp extends JFrame {

    private JLabel photographLabel = new JLabel();
    private JToolBar buttonBar1 = new JToolBar();
    private JToolBar buttonBar2 = new JToolBar();
    private JToolBar buttonBar3 = new JToolBar();
    private JPanel mDescPanel = new JPanel();
    private JPanel thumbPane1 = new JPanel();
    
    
    private GenreDAO genreDAO = new GenreDAO();
    private List<MovieTO> movieTOList1 = null;
    private List<MovieTO> movieTOList2 = null;
    private List<MovieTO> movieTOList3 = null;
    
    private int movieCount = 10;

    private MissingIcon placeholderIcon = new MissingIcon();

    /**
     * Main entry point to the demo. Loads the Swing elements on the "Event
     * Dispatch Thread".
     *
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    IconDemoApp app = new IconDemoApp();
                    app.setVisible(true);
                }
            });
    }

    /**
     * Default constructor for the demo.
     */
    public IconDemoApp() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Icon Demo: Please Select an Image");

        thumbPane1.setLayout(new BoxLayout(thumbPane1, BoxLayout.Y_AXIS));
        
        //add photographLabel to mDescPanel
        mDescPanel.add(photographLabel, BorderLayout.WEST);
        
        // A label for displaying the pictures
        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // We add two glue components. Later in process() we will add thumbnail buttons
        // to the toolbar inbetween thease glue compoents. This will center the
        // buttons in the toolbar.
        buttonBar1.add(Box.createGlue());
        buttonBar1.add(Box.createGlue());
        
        buttonBar2.add(Box.createGlue());
        buttonBar2.add(Box.createGlue());
        
        buttonBar3.add(Box.createGlue());
        buttonBar3.add(Box.createGlue());
        
        //Add Thumbnail toolbars to the scrollPane
        thumbPane1.add(buttonBar1);
        thumbPane1.add(buttonBar2);
        //thumbPane1.add(buttonBar3);

        add(thumbPane1, BorderLayout.SOUTH);
       
        add(mDescPanel, BorderLayout.NORTH);

        // Load the main window in a maximize state        
        setExtendedState(MAXIMIZED_BOTH);

        // this centers the frame on the screen
        setLocationRelativeTo(null);

        //download 8 movies per genre
        movieTOList1 = genreDAO.getMoviesByGenre(28, movieCount);
        movieTOList2 = genreDAO.getMoviesByGenre(99, movieCount);
        movieTOList3 = genreDAO.getMoviesByGenre(12, movieCount);

        // start the image loading SwingWorker in a background thread
        loadimages.execute();
    }

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
        protected Void doInBackground() throws Exception 
        {
            MovieTO movieTO = null;
            String title = null;

            for (int i = 0; i < movieCount; i++) {
                ImageIcon icon;
                movieTO = movieTOList1.get(i);
                title = movieTO.getTitle();
                
                String imgUrl =
                    Constant.TMDb_IMG_URL + movieTO.getPosterPath();
                
                icon = createImageIcon(imgUrl, title);
                //System.out.println(imgUrl);
                
                ThumbnailAction thumbAction;
                if (icon != null) {

                    ImageIcon thumbnailIcon =
                        new ImageIcon(getScaledImage(icon.getImage(), 100,
                                                     150));

                    thumbAction =
                            new ThumbnailAction(icon, thumbnailIcon, title);

                } else {
                    // the image failed to load for some reason
                    // so load a placeholder instead
                    thumbAction =
                            new ThumbnailAction(placeholderIcon, placeholderIcon,
                                                title);
                }
                publish(thumbAction);
            }
            // unfortunately we must return something, and only null is valid to
            // return when the return type is void.
            return null;
        }

        /**
         * Process all loaded images.
         */
        @Override
        protected void process(List<ThumbnailAction> chunks) {
            for (ThumbnailAction thumbAction : chunks) {
                JButton thumbButton = new JButton(thumbAction);
                // add the new button BEFORE the last glue
                // this centers the buttons in the toolbar
                buttonBar1.add(thumbButton, buttonBar1.getComponentCount() - 1);
                buttonBar2.add(thumbButton, buttonBar2.getComponentCount() - 1);
            }
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
            icon.setImage(getScaledImage(icon.getImage(), 200, 300));
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
            photographLabel.setIcon(displayPhoto);
            setTitle("Icon Demo: " + getValue(SHORT_DESCRIPTION).toString());
        }
    }
}
