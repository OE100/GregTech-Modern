package com.gregtechceu.gtceu.client.renderer.pipe.quad;

import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IQuadTransformer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import lombok.Getter;
import lombok.Setter;

import static net.minecraftforge.client.model.IQuadTransformer.*;

@OnlyIn(Dist.CLIENT)
public class RecolorableBakedQuad extends BakedQuad {

    private final SpriteInformation spriteInformation;
    private final VertexFormat format;

    public RecolorableBakedQuad(int[] unpackedData, int tint, Direction orientation,
                                SpriteInformation texture, boolean applyDiffuseLighting, boolean hasAmbientOcclusion,
                                VertexFormat format) {
        super(unpackedData, tint, orientation, texture.sprite(), applyDiffuseLighting, hasAmbientOcclusion);
        this.spriteInformation = texture;
        this.format = format;
    }

    public RecolorableBakedQuad withColor(ColorData data) {
        if (!spriteInformation.colorable()) return this;
        int argb = data.colorsARGB()[spriteInformation.colorID()];

        int[] newData = new int[format.getIntegerSize() * 4];

        float a = ((argb >> 24) & 0xFF) / 255f; // alpha
        float r = ((argb >> 16) & 0xFF) / 255f; // red
        float g = ((argb >> 8) & 0xFF) / 255f; // green
        float b = ((argb) & 0xFF) / 255f; // blue
        float[] array = new float[] { r, g, b, a };
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < format.getElements().size(); e++) {
                if (format.getElements().get(e).getUsage() == VertexFormatElement.Usage.COLOR) {
                    newData[v * format.getIntegerSize() + IQuadTransformer.COLOR] = argb;
                } else {
                    int offset = findOffset(format.getElements().get(e));
                    newData[v * format.getIntegerSize() + offset] = this.vertices[v * format.getIntegerSize() + offset];
                }
            }
        }
        return new RecolorableBakedQuad(newData, this.tintIndex, this.direction, this.spriteInformation,
                this.isShade(), this.hasAmbientOcclusion(), this.format);
    }

    public static RecolorableBakedQuad.Builder of(BakedQuad quad, VertexFormat format) {
        Builder builder = new Builder(format);
        QuadHelper.putBakedQuad(builder, quad);
        return builder;
    }

    public static class Builder implements VertexConsumer {

        @Getter
        private final VertexFormat vertexFormat;
        private final int[] unpackedData;
        @Setter
        private int quadTint = -1;
        @Setter
        private Direction quadOrientation;
        @Setter
        private SpriteInformation texture;
        @Setter
        private boolean shade = true;
        @Setter
        private boolean hasAmbientOcclusion = true;

        private int vertices = 0;
        private int elements = 0;
        private boolean full = false;
        private boolean contractUVs = false;

        public Builder(VertexFormat vertexFormat) {
            this.vertexFormat = vertexFormat;
            unpackedData = new int[vertexFormat.getIntegerSize() * 4];
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            int offset = vertices * vertexFormat.getIntegerSize() + POSITION;
            unpackedData[offset] = Float.floatToRawIntBits((float) x);
            unpackedData[offset + 1] = Float.floatToRawIntBits((float) y);
            unpackedData[offset + 2] = Float.floatToRawIntBits((float) z);
            addElement();
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            int offset = vertices * vertexFormat.getIntegerSize() + COLOR;
            unpackedData[offset] = ((alpha & 0xFF) << 24) |
                    ((blue & 0xFF) << 16) |
                    ((green & 0xFF) << 8) |
                    (red & 0xFF);
            addElement();
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            int offset = vertices * vertexFormat.getIntegerSize() + UV0;
            unpackedData[offset] = Float.floatToRawIntBits(u);
            unpackedData[offset + 1] = Float.floatToRawIntBits(v);
            addElement();
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            if (UV1 >= 0) {
                int offset = vertices * vertexFormat.getIntegerSize() + UV1;
                unpackedData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
                addElement();
            }
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            int offset = vertices * vertexFormat.getIntegerSize() + UV2;
            unpackedData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
            addElement();
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            int offset = vertices * vertexFormat.getIntegerSize() + NORMAL;
            unpackedData[offset] = ((int) (x * 127.0f) & 0xFF) |
                    (((int) (y * 127.0f) & 0xFF) << 8) |
                    (((int) (z * 127.0f) & 0xFF) << 16);
            addElement();
            return this;
        }

        @Override
        public void endVertex() {}

        @Override
        public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {}

        @Override
        public void unsetDefaultColor() {}

        public void put(int element, int... data) {
            for (int i = 0; i < 4; i++) {
                if (i < data.length) {
                    unpackedData[vertices * vertexFormat.getIntegerSize() + element] = data[i];
                } else {
                    unpackedData[vertices * vertexFormat.getIntegerSize() + element] = 0;
                }
            }
            addElement();
        }

        private void addElement() {
            elements++;
            if (elements == vertexFormat.getElements().size()) {
                vertices++;
                elements = 0;
            }
            if (vertices == 4) {
                full = true;
            }
        }

        public RecolorableBakedQuad build() {
            if (!full) {
                throw new IllegalStateException("not enough data");
            }
            if (texture == null) {
                throw new IllegalStateException("texture not set");
            }
            if (contractUVs) {
                float tX = texture.sprite().contents().width() / (texture.sprite().getU1() - texture.sprite().getU0());
                float tY = texture.sprite().contents().height() / (texture.sprite().getV1() - texture.sprite().getV0());
                float tS = Math.max(tX, tY);
                float ep = 1f / (tS * 0x100);
                int uve = 0;
                while (uve < vertexFormat.getElements().size()) {
                    VertexFormatElement e = vertexFormat.getElements().get(uve);
                    if (e.getUsage() == VertexFormatElement.Usage.UV && e.getIndex() == 0) {
                        break;
                    }
                    uve++;
                }
                if (uve == vertexFormat.getElements().size()) {
                    throw new IllegalStateException("Can't contract UVs: format doesn't contain UVs");
                }
                float[] uvc = new float[4];
                for (int v = 0; v < 4; v++) {
                    for (int i = 0; i < 4; i++) {
                        uvc[i] += Float.intBitsToFloat(unpackedData[v * uve + i] / 4);
                    }
                }
                for (int v = 0; v < 4; v++) {
                    for (int i = 0; i < 4; i++) {
                        float uo = Float.intBitsToFloat(unpackedData[v * uve + i]);
                        float eps = 1f / 0x100;
                        float un = uo * (1 - eps) + uvc[i] * eps;
                        float ud = uo - un;
                        float aud = ud;
                        if (aud < 0) aud = -aud;
                        if (aud < ep) // not moving a fraction of a pixel
                        {
                            float udc = uo - uvc[i];
                            if (udc < 0) udc = -udc;
                            if (udc < 2 * ep) // center is closer than 2 fractions of a pixel, don't move too close
                            {
                                un = (uo + uvc[i]) / 2;
                            } else // move at least by a fraction
                            {
                                un = uo + (ud < 0 ? ep : -ep);
                            }
                        }
                        unpackedData[v * uve + i] = Float.floatToRawIntBits(un);
                    }
                }
            }
            return new RecolorableBakedQuad(unpackedData, quadTint, quadOrientation, texture, shade,
                    hasAmbientOcclusion, vertexFormat);
        }
    }

    private static int findOffset(VertexFormatElement element) {
        // Divide by 4 because we want the int offset
        var index = DefaultVertexFormat.BLOCK.getElements().indexOf(element);
        return index < 0 ? -1 : DefaultVertexFormat.BLOCK.getOffset(index) / 4;
    }
}