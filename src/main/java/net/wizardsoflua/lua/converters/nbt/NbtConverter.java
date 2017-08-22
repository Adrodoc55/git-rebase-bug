package net.wizardsoflua.lua.converters.nbt;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Table;
import net.wizardsoflua.lua.table.DefaultTableBuilder;
import net.wizardsoflua.lua.table.TableIterable;

public class NbtConverter {
  public static int COMPOUND_TAG_TYPE = 10;

  public static NBTTagCompound merge(NBTTagCompound origTagCompound, Table luaData) {
    NBTTagCompound resultTagCompound = new NBTTagCompound();
    Set<String> keys = origTagCompound.getKeySet();
    Set<Object> usedKeys = new HashSet<>();
    for (String key : keys) {
      Object luaValue = luaData.rawget(key);
      if (luaValue != null) {
        usedKeys.add(key);
      } else if (luaValue == null && isANumber(key)) {
        double dval = Double.parseDouble(key);
        luaValue = luaData.rawget(dval);
        if (luaValue != null) {
          usedKeys.add(dval);
        }
      }
      NBTBase oldValue = origTagCompound.getTag(key);
      if (luaValue != null) {
        NBTBase newValue = merge(key, oldValue, luaValue);
        resultTagCompound.setTag(key, newValue);
      } else {
        resultTagCompound.setTag(key, oldValue.copy());
      }
    }
    Object tblKey = luaData.initialKey();
    while (tblKey != null) {
      String key = String.valueOf(tblKey);
      if (!usedKeys.contains(key)) {
        Object luaValue = luaData.rawget(tblKey);
        NBTBase value = toNbt(luaValue);
        resultTagCompound.setTag(key, value);
      }
      tblKey = luaData.successorKeyOf(tblKey);
    }
    return resultTagCompound;
  }

  private static NBTTagList merge(String key, NBTTagList origTagList, Table data) {
    switch (key) {
      case "Items":
      case "Inventory":
        return new ValueBasedNbtListMergeStrategy("Slot").merge(origTagList, data);
      case "Tags":
        NBTTagList result = new NBTTagList();
        for (Entry<Object, Object> entry : new TableIterable(data)) {
          result.appendTag(new NBTTagString(entry.getValue().toString()));
        }
        return result;
      default:
        return new IndexBasedNbtListMergeStrategy().merge(origTagList, data);
    }
  }

  static NBTBase merge(String key, NBTBase tag, Object value) {
    if (tag == null) {
      // ignore
      return null;
    } else if (value == null) {
      return null;
    } else if (tag instanceof NBTTagEnd) {
      // ignore
      return tag;
    } else if (tag instanceof NBTTagFloat) {
      return new NBTTagFloat(((Number) value).floatValue());
    } else if (tag instanceof NBTTagDouble) {
      return new NBTTagDouble(((Number) value).doubleValue());
    } else if (tag instanceof NBTTagLong) {
      return new NBTTagLong(((Number) value).longValue());
    } else if (tag instanceof NBTTagInt) {
      return new NBTTagInt(((Number) value).intValue());
    } else if (tag instanceof NBTTagShort) {
      return new NBTTagShort(((Number) value).shortValue());
    } else if (tag instanceof NBTTagByte) {
      if (value instanceof Boolean) {
        boolean b = ((Boolean) value).booleanValue();
        return new NBTTagByte(b ? ((byte) 1) : ((byte) 0));
      }
      return new NBTTagByte(((Number) value).byteValue());
    } else if (tag instanceof NBTTagString) {
      if (value instanceof ByteString) {
        String s = ((ByteString) value).decode();
        return new NBTTagString(s);
      } else if (value instanceof String) {
        return new NBTTagString((String) value);
      } else {
        throw new IllegalArgumentException(
            "Expected a string but got " + (value.getClass().getSimpleName()));
      }
    } else if (tag instanceof NBTTagByteArray) {
      // Do we need that? Currently not supported!
      // return toTable(((NBTTagByteArray)tag).getByteArray());
      throw new UnsupportedOperationException("Conversion of NBTTagByteArray is not supported!");
    } else if (tag instanceof NBTTagIntArray) {
      // Do we need that? Currently not supported!
      // return toTable(((NBTTagIntArray)tag).getIntArray());
      throw new UnsupportedOperationException("Conversion of NBTTagIntArray is not supported!");
    } else if (tag instanceof NBTTagList) {
      if (value instanceof Table) {
        return merge(key, (NBTTagList) tag, (Table) value);
      } else {
        throw new IllegalArgumentException(
            "Expected a table but got " + (value.getClass().getSimpleName()));
      }
    } else if (tag instanceof NBTTagCompound) {
      if (value instanceof Table) {
        return merge((NBTTagCompound) tag, (Table) value);
      } else {
        throw new IllegalArgumentException(
            "Expected a table but got " + (value.getClass().getSimpleName()));
      }
    } else {
      throw new UnsupportedOperationException(
          "Conversion is not supported for " + tag.getClass().getSimpleName());
    }
  }



  private static boolean isANumber(String txt) {
    return NumberUtils.isNumber(txt);
  }

  public static NBTTagCompound toNntCompound(Table data) {
    NBTTagCompound result = new NBTTagCompound();
    Object key = data.initialKey();
    while (key != null) {
      NBTBase value = toNbt(data.rawget(key));
      if (value != null) {
        result.setTag(String.valueOf(key), value);
      }
      key = data.successorKeyOf(key);
    }
    return result;
  }

  private static NBTBase toNbt(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Table) {
      return toNntCompound((Table) value);
    }
    if (value instanceof Double) {
      return new NBTTagDouble((Double) value);
    }
    if (value instanceof Float) {
      return new NBTTagFloat((Float) value);
    }
    if (value instanceof Long) {
      return new NBTTagLong((Long) value);
    }
    if (value instanceof Integer) {
      return new NBTTagInt((Integer) value);
    }
    if (value instanceof Short) {
      return new NBTTagShort((Short) value);
    }
    if (value instanceof Byte) {
      return new NBTTagByte((Byte) value);
    }
    if (value instanceof String) {
      return new NBTTagString((String) value);
    }
    if (value instanceof ByteString) {
      return new NBTTagString(((ByteString) value).decode());
    }
    if (value instanceof Boolean) {
      boolean b = ((Boolean) value).booleanValue();
      return new NBTTagByte(b ? ((byte) 1) : ((byte) 0));
    }
    return null;
  }

  public static void insertValues(DefaultTableBuilder builder, NBTTagCompound tagCompound) {
    checkNotNull(tagCompound, "tagCompound==null!");
    Set<String> keys = tagCompound.getKeySet();
    for (String key : keys) {
      NBTBase tag = tagCompound.getTag(key);
      Object value = toLua(tag);
      if (value != null) {
        Object luaKey = NbtPrimitiveConverter.toLua(key);
        builder.add(luaKey, value);
      }
    }
  }

  public static Object toLua(NBTBase tag) {
    if (tag == null) {
      // ignore
      return null;
    } else if (tag instanceof NBTTagEnd) {
      // ignore
      return null;
    } else if (tag instanceof NBTTagFloat) {
      return ((NBTTagFloat) tag).getDouble();
    } else if (tag instanceof NBTTagDouble) {
      return ((NBTTagDouble) tag).getDouble();
    } else if (tag instanceof NBTPrimitive) {
      return ((NBTPrimitive) tag).getLong();
    } else if (tag instanceof NBTTagString) {
      return ByteString.of((((NBTTagString) tag).getString()));
    } else if (tag instanceof NBTTagByteArray) {
      // Do we need that? Currently not supported!
      // return toTable(((NBTTagByteArray)tag).getByteArray());
      throw new UnsupportedOperationException("Conversion of NBTTagByteArray is not supported!");
    } else if (tag instanceof NBTTagIntArray) {
      return toLua(((NBTTagIntArray) tag).getIntArray());
    } else if (tag instanceof NBTTagList) {
      return toLua(((NBTTagList) tag));
    } else if (tag instanceof NBTTagCompound) {
      return toLua(((NBTTagCompound) tag));
    } else {
      throw new UnsupportedOperationException(
          "Conversion is not supported for " + tag.getClass().getSimpleName());
    }
  }

  public static Table toLua(NBTTagCompound tagCompound) {
    checkNotNull(tagCompound, "tagCompound==null!");
    DefaultTableBuilder builder = new DefaultTableBuilder();
    insertValues(builder, tagCompound);
    return builder.build();
  }

  public static Table toLua(NBTTagList list) {
    DefaultTableBuilder builder = new DefaultTableBuilder();
    int size = list.tagCount();
    for (int i = 0; i < size; ++i) {
      NBTBase tag = list.get(i);
      Object value = toLua(tag);
      if (value != null) {
        builder.add((long) (i + 1), value);
      }
    }
    return builder.build();
  }

  public static Table toLua(int[] intArray) {
    DefaultTableBuilder builder = new DefaultTableBuilder();
    for (int i = 0; i < intArray.length; ++i) {
      builder.add((long) (i + 1), intArray[i]);
    }
    return builder.build();
  }

}
