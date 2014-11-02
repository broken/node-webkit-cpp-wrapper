package com.dogatech.nodewebkitwrapper.prototype.type;

import java.util.LinkedList;
import java.util.List;

import com.dogatech.nodewebkitwrapper.grammar.nodewebkitwrapperParser;
import com.dogatech.nodewebkitwrapper.io.Outputter;
import com.dogatech.nodewebkitwrapper.prototype.CppClass;
import com.dogatech.nodewebkitwrapper.prototype.CppMethod;


public class CppTypeFactory {
  private static CppTypeFactory instance = null;
  private CppType[] types = {
    new VoidType(),
    new ResultSetIteratorType(),
    new SoulSifterModelType(),
    //TODO new VectorType(),
    new BooleanType(),
    new NumberType(),
    new StringType(),
    new TimeTType()
  };

  protected CppTypeFactory() { }

  public static CppTypeFactory instance() {
    if (instance == null) {
      instance = new CppTypeFactory();
    }
    return instance;
  }

  public CppType createType(nodewebkitwrapperParser.TypeContext ctx, CppClass cppClass, Outputter out) {
    CppType t = createType(ctx.Identifier().toString(), cppClass, out);
    if (t != null) t.isConst = ctx.CONST() != null;
    if (t != null && ctx.generic() != null) {
      List<CppType> generics = new LinkedList<CppType>();
      for (nodewebkitwrapperParser.TypeContext tc : ctx.generic().typeList().type()) {
        CppType gt = createType(tc, cppClass, out);
        if (gt == null) return null;
        generics.add(gt);
      }
      t.generics = generics;
    }
    return t;
  }

  public CppType createType(String name, CppClass cppClass, Outputter out) {
    try {
      for (CppType t : types) {
        if (t.isType(name)) {
          CppType type = t.getClass().newInstance();
          type.init(name, cppClass, out);
          return type;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}