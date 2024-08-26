package com.Salzburg_Restaurant;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import opennlp.tools.doccat.*;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;
import java.sql.DriverManager;

public class OpenNLPChatBot extends JFrame implements ActionListener, KeyListener {
    private JTextArea chatArea;
    private JTextField userInputField;
    private JButton sendButton;

    private Map<String, String> questionAnswer;

    private DoccatModel model;

    public OpenNLPChatBot() {
        setTitle("Salzburg Restaurants limited");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1900, 900);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(34, 40, 49)); 

        Border border = BorderFactory.createLineBorder(Color.BLACK, 1);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBorder(border);
        chatArea.setBackground(new Color(0, 255, 204)); 
        chatArea.setForeground(Color.BLACK); 
        chatArea.setFont(new Font("Arial", Font.BOLD, 25)); 
        chatArea.setLineWrap(true); 
        chatArea.setWrapStyleWord(true); 
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);



        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(0, 102, 204)); 
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        userInputField = new JTextField();
        userInputField.setBorder(border);
        userInputField.setForeground(Color.WHITE); 
        userInputField.setBackground(new Color(46, 49, 49));
        userInputField.setPreferredSize(new Dimension(300, 50)); 
        userInputField.setFont(new Font("Arial", Font.BOLD, 20)); 
        userInputField.addKeyListener(this); 
        inputPanel.add(userInputField, BorderLayout.CENTER);
        sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        sendButton.setBorder(border);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        

        questionAnswer = new HashMap<>();
        initializeQuestionAnswerMap();

        try {
            model = trainCategorizerModel();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to initialize chatbot model.");
            System.exit(1);
        }
    }

    private void initializeQuestionAnswerMap() {
    	questionAnswer.put("error-message", "Sorry, what you have entered is not recognizable");
		questionAnswer.put("greeting", "Hello, how can I help you?");
		questionAnswer.put("menu-list", "1. Pizza\n2. Sushi\n3. Vegan Burger\n4. Salad\n5. Ice Cream\n6. Coffee\n7. Taco\n8. Pasta\n9. Ramen\n10. Chocolate Cake\n11. Smoothie\n12. Quinoa Salad\n13. BBQ Ribs\n14. Cheesecake\n15. Muffin\n16. Noodle\n17. Tea");
		questionAnswer.put("pizza-description", "Our pizza is made with a hand-tossed crust, fresh tomato sauce, mozzarella, and your choice of toppings.");
		questionAnswer.put("pizza-price", "Price starts at $12 for a medium size. Extra toppings are $2 each.");

		questionAnswer.put("sushi-description", "Our sushi platter includes an assortment of nigiri, maki, and sashimi, prepared with fresh, sustainably sourced fish.");
		questionAnswer.put("sushi-price", "Sushi platter prices start at $20 for 12 pieces.");

		questionAnswer.put("vegan-burger-description", "Our vegan burger is made with a 100% plant-based patty, topped with lettuce, tomato, and our special sauce, all on a vegan bun.");
		questionAnswer.put("vegan-burger-price", "Each vegan burger is $15. Add vegan cheese for an extra $1.");

		questionAnswer.put("salad-description", "Our garden salad includes mixed greens, cherry tomatoes, cucumbers, carrots, and your choice of dressing.");
		questionAnswer.put("salad-price", "A large garden salad is $10. Add grilled chicken or tofu for $3.");

		questionAnswer.put("ice-cream-description", "Choose from over 20 flavors of our homemade ice cream. Vegan options available.");
		questionAnswer.put("ice-cream-price", "One scoop is $3, two scoops for $5. Add toppings for $0.50 each.");

		questionAnswer.put("coffee-description", "Our coffee is brewed from organic, fair-trade beans. Available in regular or decaf.");
		questionAnswer.put("coffee-price", "A regular coffee is $2.50. Specialty coffees start at $4.");
		questionAnswer.put("taco-description", "Our tacos come with your choice of filling: grilled chicken, beef, or veggies, topped with fresh salsa, cheese, and lettuce.");
		questionAnswer.put("taco-price", "Each taco is $3. Vegan cheese available for an extra $1.");

		questionAnswer.put("pasta-description", "Choose from spaghetti, fettuccine, or penne, served with our signature marinara or Alfredo sauce.");
		questionAnswer.put("pasta-price", "Pasta dishes start at $15. Add meatballs or grilled chicken for $4.");

		questionAnswer.put("ramen-description", "Authentic Japanese ramen with a rich, flavorful broth, noodles, sliced pork, seaweed, and a soft-boiled egg.");
		questionAnswer.put("ramen-price", "A bowl of ramen is $12. Add extra noodles for $2.");

		questionAnswer.put("chocolate-cake-description", "Our chocolate cake is rich and moist, layered with chocolate ganache and topped with fresh berries.");
		questionAnswer.put("chocolate-cake-price", "A slice of chocolate cake is $7. Whole cakes available for $40.");

		questionAnswer.put("smoothie-description", "Freshly blended smoothies made with your choice of fruits, vegetables, and a base of water, dairy milk, or almond milk.");
		questionAnswer.put("smoothie-price", "Smoothies start at $5. Add protein powder or supplements for $1 each.");
		
		questionAnswer.put("quinoa-salad-description", "A hearty salad with quinoa, mixed greens, avocado, cherry tomatoes, cucumber, and a lemon vinaigrette.");
		questionAnswer.put("quinoa-salad-price", "A large quinoa salad is $13. Add feta cheese or grilled chicken for $3.");

		questionAnswer.put("cheesecake-description", "Creamy cheesecake on a graham cracker crust, with your choice of strawberry, blueberry, or raspberry topping.");
		questionAnswer.put("cheesecake-price", "A slice of cheesecake is $6. Whole cheesecakes available for $35.");
		questionAnswer.put("conversation-continue", "Do you have any other questions?");
		questionAnswer.put("conversation-complete", "Thank you!! Do come back soon");
		questionAnswer.put("emotional-response", "Thats great!!");
		questionAnswer.put("thankful-response", "We are at your service");
		questionAnswer.put("feedback", "Please give us your feedback");
		questionAnswer.put("complaint", "Ok noted");
		questionAnswer.put("taco-description", "Our tacos come with your choice of filling: grilled chicken, beef, or veggies, topped with fresh salsa, cheese, and lettuce.");
		questionAnswer.put("taco-price", "Each taco is $3. Vegan cheese available for an extra $1.");

		questionAnswer.put("cake-description", "Enjoy our delicious cakes, available in various flavors including chocolate, vanilla, and red velvet, all frosted with our homemade buttercream.");
		questionAnswer.put("cake-price", "Price starts at $25 for a small cake. Custom decorations available for an additional charge.");

		questionAnswer.put("smoothie-description", "Freshly blended smoothies made with your choice of fruits, vegetables, and a base of water, dairy milk, or almond milk.");
		questionAnswer.put("smoothie-price", "Smoothies start at $5. Add protein powder or supplements for $1 each.");

		questionAnswer.put("pasta-description", "Choose from spaghetti, fettuccine, or penne, served with our signature marinara or Alfredo sauce.");
		questionAnswer.put("pasta-price", "Pasta dishes start at $15. Add meatballs or grilled chicken for $4.");

		questionAnswer.put("noodle-description", "Our noodle dishes feature options like soba, udon, or rice noodles, served with a variety of sauces and fresh vegetables.");
		questionAnswer.put("noodle-price", "Noodle dishes start at $14. Add your choice of protein for an extra $3.");

		questionAnswer.put("tea-description", "We offer a selection of teas, including green, black, herbal, and chai, all sourced from organic tea gardens.");
		questionAnswer.put("tea-price", "A cup of tea is $2. Specialty tea blends start at $3.");

		questionAnswer.put("muffin-description", "Our muffins are baked fresh daily. Choose from blueberry, chocolate chip, bran, and more.");
		questionAnswer.put("muffin-price", "Each muffin is $2.50. Buy a half dozen for $12.");
		questionAnswer.put("identity", "We are Salzburg restaurants located 30 m south of Brentford Estate Turing street. We are experienced in serving our customers and leaving them satisfied after every visit.");
    }

 
    private DoccatModel trainCategorizerModel() throws IOException {
        InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(new File("faq-categorizer.txt"));
        ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
        ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
        DoccatFactory factory = new DoccatFactory(new FeatureGenerator[]{new BagOfWordsFeatureGenerator()});
        TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
        params.put(TrainingParameters.CUTOFF_PARAM, 0);
        return DocumentCategorizerME.train("en", sampleStream, params, factory);
    }
    private void sendMessage() {
        String userInput = userInputField.getText().trim();
        if (!userInput.isEmpty()) {
            try {
                String[] sentences = breakSentences(userInput);
                String answer = "";
                boolean conversationComplete = false;
                for (String sentence : sentences) {
                    String[] tokens = tokenizeSentence(sentence);
                    String[] posTags = detectPOSTags(tokens);
                    String[] lemmas = lemmatizeTokens(tokens, posTags);
                    String category = detectCategory(model, lemmas);
                    answer = answer + " " + questionAnswer.getOrDefault(category, "I'm sorry, I don't understand that.") + "\n";
                    if ("conversation-complete".equals(category)) {
                        conversationComplete = true;
                    }
                }
                chatArea.append("You: " + userInput + "\n");
                chatArea.append("Bot: " + answer + "\n\n"); 
                if (conversationComplete) {
                    userInputField.setEditable(false);
                    sendButton.setEnabled(false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while processing your request.");
            }
            userInputField.setText("");
        }
    }


    private String[] breakSentences(String data) throws IOException {
        try (InputStream modelIn = new FileInputStream("en-sent.bin")) {
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(new SentenceModel(modelIn));
            return sentenceDetector.sentDetect(data);
        }
    }

    private String[] tokenizeSentence(String sentence) throws IOException {
        try (InputStream modelIn = new FileInputStream("en-token.bin")) {
            Tokenizer tokenizer = new TokenizerME(new TokenizerModel(modelIn));
            return tokenizer.tokenize(sentence);
        }
    }

    private String[] detectPOSTags(String[] tokens) throws IOException {
        try (InputStream modelIn = new FileInputStream("en-pos-maxent.bin")) {
            POSTaggerME posTagger = new POSTaggerME(new POSModel(modelIn));
            return posTagger.tag(tokens);
        }
    }

    private String[] lemmatizeTokens(String[] tokens, String[] posTags) throws IOException {
        try (InputStream modelIn = new FileInputStream("en-lemmatizer.bin")) {
            LemmatizerME lemmatizer = new LemmatizerME(new LemmatizerModel(modelIn));
            return lemmatizer.lemmatize(tokens, posTags);
        }
    }

    private String detectCategory
    (DoccatModel model, String[] finalTokens) throws IOException {
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);
        double[] probabilitiesOfOutcomes = categorizer.categorize(finalTokens);
        return categorizer.getBestCategory(probabilitiesOfOutcomes);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            sendMessage();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            sendButton.doClick();
        }
    }

    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OpenNLPChatBot chatBotGUI = new OpenNLPChatBot();
            chatBotGUI.setVisible(true);
        });
    }
}
