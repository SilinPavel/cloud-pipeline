import org.apache.commons.collections4.ListUtils;
import java.util.List;
    public static final String NEW_FILE_HEADER_MESSAGE = "new file mode";
    public static final String DELETED_FILE_HEADER_MESSAGE = "deleted file mode";
    public static DiffType defineDiffType(final Diff diff) {
        if (isFileWasCreated(diff)) {
            return DiffType.ADDED;
        } else if (isFileWasDeleted(diff)) {
            return DiffType.DELETED;
        } else if (!diff.getFromFileName().equals(diff.getToFileName())) {
            return DiffType.RENAMED;
        } else {
            return DiffType.CHANGED;
        }
    }

    private static boolean isFileWasDeleted(Diff diff) {
        return !diff.getFromFileName().equals(DEV_NULL) && diff.getToFileName().equals(DEV_NULL)
                || ListUtils.emptyIfNull(diff.getHeaderLines()).stream()
                        .anyMatch(h -> h.contains(DELETED_FILE_HEADER_MESSAGE));
    private static boolean isFileWasCreated(Diff diff) {
        return diff.getFromFileName().equals(DEV_NULL) && !diff.getToFileName().equals(DEV_NULL)
                || ListUtils.emptyIfNull(diff.getHeaderLines()).stream()
                        .anyMatch(h -> h.contains(NEW_FILE_HEADER_MESSAGE));
    public static String getChangedFileName(final Diff diff) {

    public static boolean isBinary(final Diff diff, final List<String> binaryExts) {
        return ListUtils.emptyIfNull(diff.getHunks()).isEmpty() ||
                binaryExts.stream()
                        .anyMatch(ext -> diff.getToFileName().endsWith(ext) || diff.getFromFileName().endsWith(ext));
    }

    public enum DiffType {
        ADDED, DELETED, CHANGED, RENAMED
    }