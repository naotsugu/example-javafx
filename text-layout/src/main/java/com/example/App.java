package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.FontSmoothingType;
import javafx.stage.Stage;
import com.sun.javafx.geom.Point2D;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.scene.text.*;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import java.util.HashMap;
import java.util.Map;

public class App extends Application {

    private final TextLayout textLayout = Toolkit.getToolkit().getTextLayoutFactory().createLayout();
    private final Canvas canvas = new Canvas();
    private final Map<Object, Font> fontMap = new HashMap<>();

    @Override
    public void start(Stage stage) {

        StackPane stackPane = new StackPane(canvas);
        canvas.widthProperty().bind(stackPane.widthProperty());
        canvas.heightProperty().bind(stackPane.heightProperty());
        canvas.widthProperty().addListener((observable, oldValue, newValue) ->
            draw(newValue.doubleValue(), canvas.getHeight()));
        canvas.heightProperty().addListener((observable, oldValue, newValue) ->
            draw(canvas.getWidth(), newValue.doubleValue()));

        Scene scene = new Scene(stackPane, 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    private void draw(double width, double height) {

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.setFontSmoothingType(FontSmoothingType.LCD);

        TextSpan[] textSpans = createTextSpans();
        textLayout.setWrapWidth((float) width);
        textLayout.setContent(textSpans);

        TextSpan currentSpan = null;
        int offset = 0;

        for (TextLine line : textLayout.getLines()) {
            for (GlyphList run : line.getRuns()) {

                Point2D location = run.getLocation();
                double baselineOffset = -run.getLineBounds().getMinY();
                if (baselineOffset + location.y > height) {
                    return;
                }

                if (currentSpan != run.getTextSpan()) {
                    currentSpan = run.getTextSpan();
                    offset = 0;
                    gc.setFont(fontMap.get(currentSpan.getFont()));
                }

                int start = offset;
                offset += run.getCharOffset(run.getGlyphCount());
                String str = currentSpan.getText().substring(start, offset).replace("\n", "");

                gc.fillText(str, location.x, baselineOffset + location.y);
            }
        }
    }

    private TextSpan[] createTextSpans() {
        String text = """
            秋の田の かりほの庵の 苫をあらみ　わが衣手は 露にぬれつつ
            春過ぎて 夏来にけらし 白妙の　衣ほすてふ 天の香具山
            あしびきの 山鳥の尾の しだり尾の　長々し夜を 独りかも寝む
            田子の浦に うち出でてみれば 白妙の　富士の高嶺に 雪は降りつつ
            奥山に 紅葉踏み分け 鳴く鹿の　声聞く時ぞ 秋は悲しき
            かささぎの 渡せる橋に おく霜の　白きを見れば 夜ぞ更けにける
            天の原 ふりさけ見れば 春日なる　三笠の山に 出でし月かも
            我が庵は 都の辰巳 しかぞ住む　世をうぢ山と 人はいふなり
            花の色は 移りにけりな いたづらに　我が身世にふる ながめせし間に
            これやこの 行くも帰るも 別れては　知るも知らぬも 逢坂の関
            """;

        record Span(String getText, Object getFont, RectBounds getBounds) implements TextSpan {
        }

        String[] lines = text.split("(?<=\\n)");
        TextSpan[] spans = new TextSpan[lines.length];
        for (int i = 0; i < lines.length; i++) {
            var font = Font.font(25 - i);
            var nativeFont = FontHelper.getNativeFont(font);
            fontMap.put(nativeFont, font);
            spans[i] = new Span(lines[i], nativeFont, null);
        }
        return spans;
    }

    public static void main(String[] args) {
        launch();
    }

}
