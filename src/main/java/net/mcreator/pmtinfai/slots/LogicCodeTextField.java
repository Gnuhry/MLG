package net.mcreator.pmtinfai.slots;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class LogicCodeTextField extends TextFieldWidget {
    public LogicCodeTextField(FontRenderer fontIn, int p_i51137_2_, int p_i51137_3_, int p_i51137_4_, int p_i51137_5_, String msg) {
        super(fontIn, p_i51137_2_, p_i51137_3_, p_i51137_4_, p_i51137_5_, msg);
    }


    @Override
    public void tick() {
        super.tick();
        System.out.println(this.getText());
    }
}
