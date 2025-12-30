package ph.benchmark.netty;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.RecomputeFieldValue.Kind;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

import java.util.function.Predicate;

/*
@TargetClass(className = "io.netty.util.internal.CleanerJava6")
final class TargetCleanerJava6 {
	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, declClassName = "java.nio.DirectByteBuffer", name = "cleaner")
	private static long CLEANER_FIELD_OFFSET;
}
*/

/*
@TargetClass(className = "io.netty.util.internal.PlatformDependent0")
final class TargetPlatformDependent0 {
	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, declClassName = "java.nio.Buffer", name = "address")
	private static long ADDRESS_FIELD_OFFSET;
}
*/

/*
@TargetClass(io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess.class)
final class TargetUnsafeRefArrayAccess {
	@Alias
	@RecomputeFieldValue(kind = RecomputeFieldValue.Kind.ArrayIndexShift, declClass = Object[].class)
	public static int REF_ELEMENT_SHIFT;
}
*/

@TargetClass(className = "io.netty.buffer.AbstractReferenceCountedByteBuf", onlyWith = PlatformHasClass.class)
final class Target_io_netty_buffer_AbstractReferenceCountedByteBuf {

    @Alias
    @RecomputeFieldValue(kind = Kind.FieldOffset, //
            declClassName = "io.netty.buffer.AbstractReferenceCountedByteBuf", //
            name = "refCnt") //
    private static long REFCNT_FIELD_OFFSET;
}

@TargetClass(className = "io.netty.util.AbstractReferenceCounted", onlyWith = PlatformHasClass.class)
final class Target_io_netty_util_AbstractReferenceCounted {

    @Alias
    @RecomputeFieldValue(kind = Kind.FieldOffset, //
            declClassName = "io.netty.util.AbstractReferenceCounted", //
            name = "refCnt") //
    private static long REFCNT_FIELD_OFFSET;
}

/**
 * A predicate to tell whether this platform includes the argument class.
 */
final class PlatformHasClass implements Predicate<String> {

    @Override
    public boolean test(String className) {
        try {
            @SuppressWarnings({"unused"}) final Class<?> classForName = Class.forName(className);
            return true;
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
    }
}

@TargetClass(io.netty.util.internal.logging.InternalLoggerFactory.class)
final class TargetInternalLoggerFactory {

    @Substitute
    private static InternalLoggerFactory newDefaultFactory(String name) {
        return JdkLoggerFactory.INSTANCE;
    }
}

public class NettySubstitutions {

}
