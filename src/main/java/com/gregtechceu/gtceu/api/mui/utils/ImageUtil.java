package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ImageUtil {

    private static final List<ImageType> TYPES = new ArrayList<>();
    private static final byte[] buffer = new byte[256];

    public static final long ERROR_NO_RESOURCE = -1;
    public static final long ERROR_NO_IMAGE_TYPE = -2;
    public static final long ERROR_IO_EXCEPTION = -3;
    public static final long ERROR_PNG = -4;
    public static final long ERROR_JPEG_1 = -5;
    public static final long ERROR_JPEG_2 = -6;

    private static final String[] ERROR_MSG = {
            "Resource not found",
            "Unsupported file type",
            "Failed to parse image with unknown cause",
            "PNG file ended too early",
            "JPEG started again within itself",
            "JPEG ended too early"
    };

    public static Resource getResource(ResourceLocation resLoc) {
        try {
            return Minecraft.getInstance().getResourceManager().getResourceOrThrow(resLoc);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Parses the image size from a resource. The returned packed size can be unpacked with {@link #getWidth(long)} and
     * {@link #getHeight(long)}. If it failed a negative value is returned. Negative values can be translated into an
     * error
     * message using {@link #getError(long)}.
     *
     * @param resLoc resource location of the image
     * @return packed size or negative error value
     */
    public static long readImageSize(ResourceLocation resLoc) {
        Resource res = getResource(resLoc);
        return res == null ? ERROR_NO_RESOURCE : readImageSize(res);
    }

    public static String getError(long size) {
        return size < 0 ? ERROR_MSG[(int) (-size - 1)] : null;
    }

    /**
     * Parses the image size from a resource. The returned packed size can be unpacked with {@link #getWidth(long)} and
     * {@link #getHeight(long)}. If it failed a negative value is returned. Negative values can be translated into an
     * error
     * message using {@link #getError(long)}. A resource can be obtained with {@link #getResource(ResourceLocation)}.
     *
     * @param resource resource to read image size from
     * @return packed size or negative error value
     */
    public static long readImageSize(Resource resource) {
        try (InputStream inputStream = resource.open()) {
            return readImageSize(inputStream);
        } catch (IOException e) {
            return ERROR_IO_EXCEPTION;
        }
    }

    /**
     * Parses the image size from the input stream. The returned packed size can be unpacked with
     * {@link #getWidth(long)} and
     * {@link #getHeight(long)}. If it failed a negative value is returned or an exception is thrown.
     * Negative values can be translated into an error message using {@link #getError(long)}.
     *
     * @param inputStream bytes to read from
     * @return packed size or negative error value
     * @throws IOException if the input stream is not a valid image file
     */
    public static long readImageSize(InputStream inputStream) throws IOException {
        ImageType type = parseImageType(inputStream);
        return type == null ? ERROR_NO_IMAGE_TYPE : type.parse(inputStream);
    }

    public static long packSize(int width, int height) {
        return width | (long) height << 32;
    }

    public static int getWidth(long packedSize) {
        return (int) (packedSize & 0xFFFFFFFFL);
    }

    public static int getHeight(long packedSize) {
        return (int) ((packedSize >> 32) & 0xFFFFFFFFL);
    }

    public static boolean testImageSize(ResourceLocation resLoc, int width, int height) {
        long size = ImageUtil.readImageSize(resLoc);
        if (size < 0) {
            GTCEu.LOGGER.error("{} for location '{}'", getError(size), resLoc);
            return false;
        }
        int w = ImageUtil.getWidth(size);
        int h = ImageUtil.getHeight(size);
        if (w != width || h != height) {
            GTCEu.LOGGER.error("Image size is incorrect of image '{}'. Expected {}|{}, but actually is {}|{}", resLoc,
                    width, height, w, h);
        } else {
            GTCEu.LOGGER.info("Image '{}' has correct size", resLoc);
        }
        return true;
    }

    private static ImageType parseImageType(InputStream inputStream) throws IOException {
        if (TYPES.isEmpty()) initImageTypes();
        int bytesRead = 0;
        for (ImageType type : TYPES) {
            while (type.signatureLength > bytesRead) {
                buffer[bytesRead] = (byte) inputStream.read();
                bytesRead++;
            }
            if (startsWith(buffer, bytesRead, type.signatureStart)) {
                return type;
            }
        }
        return null;
    }

    public static String getImageType(InputStream inputStream) {
        try {
            ImageType type = parseImageType(inputStream);
            return type == null ? null : type.name();
        } catch (IOException e) {
            return null;
        }
    }

    private static void initImageTypes() {
        // puts image types into the list sorted by signature length
        // this allows checking for all image types without using PushbackInputStream
        TYPES.clear();
        ImageType[] types = ImageType.values();
        TYPES.add(types[0]);
        for (int i = 1; i < types.length; i++) {
            for (int j = 0; j < TYPES.size(); j++) {
                if (TYPES.get(j).signatureLength > types[i].signatureLength) {
                    TYPES.add(j, types[i]);
                    break;
                }
            }
        }
    }

    public static DataInput getDataInput(InputStream is) {
        return is instanceof DataInput dataInput ? dataInput : new DataInputStream(is);
    }

    public static byte[] toBytes(int... ints) {
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            bytes[i] = (byte) (ints[i] & 0xFF);
        }
        return bytes;
    }

    private static boolean startsWith(byte[] bytes, int bytesLen, byte[] startsWith) {
        if (startsWith.length > bytesLen) return false;
        for (int i = 0; i < startsWith.length; i++) {
            if (startsWith[i] != bytes[i]) return false;
        }
        return true;
    }

    public static String getFileTypeOfPath(String path) {
        int i = path.lastIndexOf('.');
        return i < 0 || i == path.length() - 1 ? null : path.substring(i + 1);
    }

    public static int readLittleEndianShort(InputStream inputStream) throws IOException {
        // DataInputStream reads big endian shorts
        return (inputStream.read() & 0xFF) | ((inputStream.read() & 0xFF) << 8);
    }

    private interface SizeParser {

        long parse(InputStream inputStream) throws IOException;
    }

    private enum ImageType implements SizeParser {

        PNG(8, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) {

            @Override
            public long parse(InputStream inputStream) throws IOException {
                DataInput dataInput = getDataInput(inputStream);
                int skipped = dataInput.skipBytes(8); // IHDR length (4), IHDR type (4)
                if (skipped != 8) return ERROR_PNG;
                return packSize(dataInput.readInt(), dataInput.readInt()); // width and height
            }
        },
        JPEG(2, 0xFF, 0xD8) {

            @Override
            public long parse(InputStream inputStream) throws IOException {
                DataInput dis = getDataInput(inputStream);

                while (true) {
                    int marker = dis.readUnsignedShort();

                    // Skip padding FFs
                    while (marker == 0xFFFF) {
                        marker = dis.readUnsignedByte();
                    }

                    // SOFn markers that contain size:
                    if (marker >= 0xFFC0 && marker <= 0xFFC3 ||
                            marker >= 0xFFC5 && marker <= 0xFFC7 ||
                            marker >= 0xFFC9 && marker <= 0xFFCB ||
                            marker >= 0xFFCD && marker <= 0xFFCF) {

                        dis.readUnsignedShort(); // block length
                        dis.readUnsignedByte();  // sample precision (8 bits)
                        int height = dis.readUnsignedShort();
                        int width = dis.readUnsignedShort();
                        return packSize(width, height);
                    }

                    if (marker == 0xFFD8) return ERROR_JPEG_1;
                    if (marker == 0xFFD9) return ERROR_JPEG_2;
                    if (marker >= 0xFFD0 && marker <= 0xFFD7 || marker == 0xFF01) continue; // no payload

                    // read length of payload and skip it
                    // length includes marker, hence -2
                    int len = dis.readUnsignedShort();
                    dis.skipBytes(len - 2);
                }
            }
        },
        // 5th byte is variable -> only use the first 4
        GIF(6, 0x47, 0x49, 0x46, 0x38) {

            @Override
            public long parse(InputStream inputStream) throws IOException {
                return packSize(readLittleEndianShort(inputStream), readLittleEndianShort(inputStream));
            }
        };

        private final int signatureLength;
        private final byte[] signatureStart;

        ImageType(int signatureLength, int... signatureStart) {
            this.signatureLength = signatureLength;
            this.signatureStart = toBytes(signatureStart);
        }
    }
}
