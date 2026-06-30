package net.hexagreen.coordboard.clipboard;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClipboardNavigationTarget implements NavigationTarget {
    private static final Pattern NAMED_VEC = Pattern.compile("x[:=]?(?<x>[+-]?\\d+)|z[:=]?(?<z>[+-]?\\d+)");
    private static final Pattern VEC3 = Pattern.compile("([+-]?\\d+)[ ,]([+-]?\\d+)[ ,]([+-]?\\d+)");
    private static final Pattern VEC2 = Pattern.compile("([+-]?\\d+)[ ,]([+-]?\\d+)");

    @Override
    public @Nullable Vec3 getTarget(NavTableBlockEntity navTableBlockEntity, ItemStack itemStack) {
        Component custom_name = itemStack.getComponents().getOrDefault(DataComponents.CUSTOM_NAME, Component.empty());
        ComponentContents c;
        int navY = 64;
        if((c = custom_name.getContents()) instanceof TranslatableContents) {
            if(((TranslatableContents) c).getKey().matches("createdeliveryrequired\\.(market|contract)\\.receipt_name")) {
                return getTargetFormDeliveryReceipt(itemStack, navY);
            }
        }
        return getTargetFromCoordinateBoard(itemStack, navY);
    }

    private @Nullable Vec3 getTargetFromCoordinateBoard(ItemStack itemStack, int y) {
        List<ClipboardEntry> currentPage = ClipboardEntry.getLastViewedEntries(itemStack);
        Optional<ClipboardEntry> selected = currentPage.stream()
            .filter(e -> e.checked)
            .filter(e -> !e.text.getString().isBlank())
            .findFirst();
        if(selected.isPresent()) {
            String string = coordinateSanitizer(selected.get().text.getString());
            try {
                Matcher namedMatcher = NAMED_VEC.matcher(string);
                if(namedMatcher.find()) {
                    String rawX = namedMatcher.group("x");
                    int x = Integer.parseInt(rawX);
                    namedMatcher.find();
                    String rawZ = namedMatcher.group("z");
                    int z = Integer.parseInt(rawZ);
                    return new Vec3(x, y, z);
                }

                Matcher v3Matcher = VEC3.matcher(string);
                if(v3Matcher.find()) {
                    int x = Integer.parseInt(v3Matcher.group(1));
                    int z = Integer.parseInt(v3Matcher.group(3));
                    return new Vec3(x, y, z);
                }

                Matcher v2Matcher = VEC2.matcher(string);
                if(v2Matcher.find()) {
                    int x = Integer.parseInt(v2Matcher.group(1));
                    int z = Integer.parseInt(v2Matcher.group(2));
                    return new Vec3(x, 64, z);
                }
            } catch(NumberFormatException ignore) {}
        }
        return null;
    }

    private @Nullable Vec3 getTargetFormDeliveryReceipt(ItemStack itemStack, int y) {
        ClipboardContent clipboardContent = itemStack.getComponents().getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
        List<ClipboardEntry> receiptPage = ClipboardEntry.readAll(clipboardContent).getFirst();
        ComponentContents coordinate = receiptPage.get(3).text.getContents();
        if(coordinate instanceof TranslatableContents) {
            Object[] vec = ((TranslatableContents) coordinate).getArgs();
            try {
                int x = Integer.parseInt(vec[0].toString());
                int z = Integer.parseInt(vec[2].toString());
                return new Vec3(x, y, z);
            } catch(NumberFormatException ignore) {}
        }
        return null;
    }

    private String coordinateSanitizer(String string) {
        return string
            .replaceAll("(?<!\\d) ", "")
            .replaceAll(" (?=,)", "")
            .toLowerCase(Locale.ENGLISH);
    }
}
