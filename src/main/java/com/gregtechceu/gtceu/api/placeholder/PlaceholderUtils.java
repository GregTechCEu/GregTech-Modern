package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.api.placeholder.exceptions.InvalidNumberException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.NotEnoughArgsException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.OutOfRangeException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.WrongNumberOfArgsException;

import java.util.List;

public class PlaceholderUtils {

    public static void checkRange(String what, int min, int max, int n) throws OutOfRangeException {
        if (n < min || n > max) throw new OutOfRangeException(what, min, max, n);
    }

    public static int toInt(MultiLineComponent component) throws InvalidNumberException {
        try {
            return component.toInt();
        } catch (NumberFormatException e) {
            throw new InvalidNumberException(component.toString());
        }
    }

    public static double toDouble(MultiLineComponent component) throws InvalidNumberException {
        try {
            return component.toDouble();
        } catch (NumberFormatException e) {
            throw new InvalidNumberException(component.toString());
        }
    }

    public static void checkArgs(List<MultiLineComponent> args, int args_num) throws WrongNumberOfArgsException {
        if (args.size() != args_num) throw new WrongNumberOfArgsException(args_num, args.size());
    }

    public static void checkArgs(List<MultiLineComponent> args, int args_num,
                                 boolean allowMore) throws NotEnoughArgsException, WrongNumberOfArgsException {
        if (!allowMore) checkArgs(args, args_num);
        if (args.size() < args_num) throw new NotEnoughArgsException(args_num, args.size());
    }

    public static long toLong(MultiLineComponent component) throws InvalidNumberException {
        try {
            return component.toLong();
        } catch (NumberFormatException e) {
            throw new InvalidNumberException(component.toString());
        }
    }
}
