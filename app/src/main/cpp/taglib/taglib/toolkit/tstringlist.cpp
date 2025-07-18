/***************************************************************************
    copyright            : (C) 2002 - 2008 by Scott Wheeler
    email                : wheeler@kde.org
 ***************************************************************************/

/***************************************************************************
 *   This library is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Lesser General Public License version   *
 *   2.1 as published by the Free Software Foundation.                     *
 *                                                                         *
 *   This library is distributed in the hope that it will be useful, but   *
 *   WITHOUT ANY WARRANTY; without even the implied warranty of            *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with this library; if not, write to the Free Software   *
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA         *
 *   02110-1301  USA                                                       *
 *                                                                         *
 *   Alternatively, this file is available under the Mozilla Public        *
 *   License Version 1.1.  You may obtain a copy of the License at         *
 *   http://www.mozilla.org/MPL/                                           *
 ***************************************************************************/

#include "tstringlist.h"

using namespace TagLib;

class StringList::StringListPrivate
{
};

////////////////////////////////////////////////////////////////////////////////
// static members
////////////////////////////////////////////////////////////////////////////////

StringList StringList::split(const String &s, const String &pattern)
{
  StringList l;

  int previousOffset = 0;
  for(int offset = s.find(pattern); offset != -1; offset = s.find(pattern, offset + 1)) {
    l.append(s.substr(previousOffset, offset - previousOffset));
    previousOffset = offset + 1;
  }

  l.append(s.substr(previousOffset, s.size() - previousOffset));

  return l;
}

////////////////////////////////////////////////////////////////////////////////
// public members
////////////////////////////////////////////////////////////////////////////////

StringList::StringList() = default;

StringList::StringList(const StringList &l) :
  List<String>(l)
{
}

StringList::StringList(std::initializer_list<String> init) :
  List<String>(init)
{
}

StringList &StringList::operator=(const StringList &l)
{
  if(this == &l)
    return *this;

  List<String>::operator=(l);
  return *this;
}

StringList &StringList::operator=(std::initializer_list<String> init)
{
  List<String>::operator=(init);
  return *this;
}

StringList::StringList(const String &s)
{
  append(s);
}

StringList::StringList(const ByteVectorList &bl, String::Type t)
{
  for(const auto &byte : bl) {
    append(String(byte, t));
  }
}

StringList::~StringList() = default;

String StringList::toString(const String &separator) const
{
  String s;

  for(auto it = begin(); it != end(); ++it) {
    s += *it;
    if(std::next(it) != end())
      s += separator;
  }

  return s;
}

StringList &StringList::append(const String &s)
{
  List<String>::append(s);
  return *this;
}

StringList &StringList::append(const StringList &l)
{
  List<String>::append(l);
  return *this;
}

////////////////////////////////////////////////////////////////////////////////
// related functions
////////////////////////////////////////////////////////////////////////////////

std::ostream &operator<<(std::ostream &s, const StringList &l)
{
  s << l.toString();
  return s;
}
