import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Stack;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;

public class HeaderWrapperListener extends nodewebkitwrapperBaseListener {
  nodewebkitwrapperParser parser;
  CppClass cppClass;
  CppNamespace cppNamespace = new CppNamespace();
  OutputStream os;

  public HeaderWrapperListener(nodewebkitwrapperParser p) {
    parser = p;
    os = System.out;
  }

  public HeaderWrapperListener(nodewebkitwrapperParser p, OutputStream out) {
    parser = p;
    os = out;
  }

  private void p(String s, boolean nl) {
    try {
      os.write(s.getBytes());
      if (nl) os.write("\n".getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void p(String s) {
    p(s, true);
  }

  @Override public void enterNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    cppNamespace.push(ctx.Identifier().toString());
  }

  @Override public void exitNamespace(@NotNull nodewebkitwrapperParser.NamespaceContext ctx) {
    cppNamespace.pop();
  }

  @Override public void enterCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    cppClass = new CppClass();
    cppClass.name = ctx.Identifier().toString();
  }

  @Override public void exitCppClass(@NotNull nodewebkitwrapperParser.CppClassContext ctx) {
    p("#ifndef " + cppClass.name + "_wrap_h");
    p("#define " + cppClass.name + "_wrap_h");
    p("");
    p("#include <node.h>");
    p("#include <nan.h>");
    p("#include \"" + cppClass.name + ".h\"");
    p("");
    p("class " + cppClass.name + " : public node::ObjectWrap {");
    p(" public:");
    p("  static void Init(v8::Handle<v8::Object> exports);");
    p("  static v8::Local<v8::Object> NewInstance();");
    p("");
    p("  void setNwcpValue(" + cppNamespace + cppClass.name + "* v) { " + cppClass.name.toLowerCase() + " = v; }");
    p("  " + cppNamespace + cppClass.name + "* getNwcpValue() const { return " + cppClass.name.toLowerCase() + "; }");
    p("");
    p(" private:");
    p("  " + cppClass.name + "();");
    p("  explicit " + cppClass.name + "(" + cppNamespace + cppClass.name + "* " + cppClass.name.toLowerCase() + ");");
    p("  ~" + cppClass.name + "();");
    p("");
    p("  static NAN_METHOD(New);");
    p("");
    for (CppMethod m : cppClass.methods) {
      boolean cannotHandleArg = false;
      for (CppType t : m.args) {
        cannotHandleArg |= t.isUnknownType(cppClass);
      }
      if (cannotHandleArg) continue;
      if (m.isGetter) {
        p("  static NAN_GETTER(" + m.name + ");");
      } else if (m.isSetter) {
        p("  static NAN_SETTER(" + m.name + ");");
      } else {
        p("  static NAN_METHOD(" + m.name + ");");
      }
    }
    p("");
    p("  static v8::Persistent<v8::Function> constructor;");
    p("  " + cppNamespace + cppClass.name + "* " + cppClass.name.toLowerCase() + ";");
    p("};");
    p("");
    p("#endif");
  }

  @Override public void enterMethod(@NotNull nodewebkitwrapperParser.MethodContext ctx) {
    cppClass.methods.add(new CppMethod(ctx));
  }


}