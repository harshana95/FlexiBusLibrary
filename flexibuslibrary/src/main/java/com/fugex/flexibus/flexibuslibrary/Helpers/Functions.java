package com.fugex.flexibus.flexibuslibrary.Helpers;

import android.graphics.Bitmap;
import android.util.Log;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import com.fugex.flexibus.flexibuslibrary.Models.Transaction;
import com.google.zxing.WriterException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Functions {

    final static String TAG = "Functions";

    public static String encodeSeatLayoutArr(int[][] seatLayout) {
        StringBuilder tmp = new StringBuilder();
        for (final int[] ints : seatLayout) {
            for (final int anInt : ints) {
                tmp.append(anInt);
                tmp.append("#");
            }
            tmp.append("/");
        }
        return tmp.toString();
    }

    public static int[][] decodeSeatLayoutString(String seatLayout) {
        char[] seatNumbersArr = seatLayout.toCharArray();
        // Find length and width
        int l = 0, w = 0;
        int tmp = 0;
        for (int i = 0; i < seatNumbersArr.length; i++) {
            if (seatNumbersArr[i] == '#') {
                tmp += 1;
            }
            if (seatNumbersArr[i] == '/') {
                l += 1;
                w = tmp;
                tmp = 0;
            }
        }
        int[][] matrix = new int[l][w];
        int idx = 0;
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < w; j++) {
                String tmpNumber = "";
                while (seatNumbersArr[idx] != '#') {
                    if (seatNumbersArr[idx] != '/') {
                        tmpNumber = tmpNumber + seatNumbersArr[idx];
                    }
                    idx++;
                }
                idx++;
                matrix[i][j] = Integer.parseInt(tmpNumber);
            }
        }
        return matrix;
    }

    public static String encodeDirectionInRoute(String route, int journeyStart, int journeyEnd) {
        if (journeyEnd - journeyStart > 0) {
            if (route.charAt(0) == '-') {
                route = route.substring(1);
            }
        } else {
            if (route.charAt(0) != '-') {
                route = '-' + route;
            }
        }
        return route;
    }

    public static String getRouteFromEncodedRoute(String route) {
        if (route.charAt(0) != '-') {
            return route;
        } else {
            return route.substring(1);
        }
    }

    public static boolean getDirectionFromEncodedRoute(String route) {
        return route.charAt(0) != '-';
    }

    public static String getDuration(Date start, Date end) {
        long duration = end.getTime() - start.getTime();
        StringBuilder stringBuilder = new StringBuilder();
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long mins = TimeUnit.MILLISECONDS.toMinutes(duration);
        if (days > 0) {
            stringBuilder.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            stringBuilder.append(hours % 24).append("h ");
        }
        stringBuilder.append(mins % 60).append("m");
        return stringBuilder.toString();
    }

    public static Bitmap getQRCode(Transaction transaction, int dimention) {
        QRGEncoder qrgEncoder = new QRGEncoder(transaction.getID(), null, QRGContents.Type.TEXT,
                dimention);
        try {
            // Getting QR-Code as Bitmap
            return qrgEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }
        return null;
    }

    public static boolean isInternetAvailable(int timeOut) {
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                @Override
                public InetAddress call() {
                    try {
                        return InetAddress.getByName("google.com");
                    } catch (UnknownHostException e) {
                        return null;
                    }
                }
            });
            inetAddress = future.get(timeOut, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return inetAddress != null && !inetAddress.equals("");
    }
}
