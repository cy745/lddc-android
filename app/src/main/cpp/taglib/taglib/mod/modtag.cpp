/***************************************************************************
    copyright           : (C) 2011 by Mathias Panzenböck
    email               : grosser.meister.morti@gmx.net
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

#include "modtag.h"

#include <utility>

#include "tstringlist.h"
#include "tpropertymap.h"

using namespace TagLib;
using namespace Mod;

class Mod::Tag::TagPrivate
{
public:
  String title;
  String comment;
  String trackerName;
};

Mod::Tag::Tag() :
  d(std::make_unique<TagPrivate>())
{
}

Mod::Tag::~Tag() = default;

String Mod::Tag::title() const
{
  return d->title;
}

String Mod::Tag::artist() const
{
  return String();
}

String Mod::Tag::album() const
{
  return String();
}

String Mod::Tag::comment() const
{
  return d->comment;
}

String Mod::Tag::genre() const
{
  return String();
}

unsigned int Mod::Tag::year() const
{
  return 0;
}

unsigned int Mod::Tag::track() const
{
  return 0;
}

String Mod::Tag::trackerName() const
{
  return d->trackerName;
}

void Mod::Tag::setTitle(const String &title)
{
  d->title = title;
}

void Mod::Tag::setArtist(const String &)
{
}

void Mod::Tag::setAlbum(const String &)
{
}

void Mod::Tag::setComment(const String &comment)
{
  d->comment = comment;
}

void Mod::Tag::setGenre(const String &)
{
}

void Mod::Tag::setYear(unsigned int)
{
}

void Mod::Tag::setTrack(unsigned int)
{
}

void Mod::Tag::setTrackerName(const String &trackerName)
{
  d->trackerName = trackerName;
}

PropertyMap Mod::Tag::properties() const
{
  PropertyMap properties;
  properties["TITLE"] = d->title;
  properties["COMMENT"] = d->comment;
  if(!(d->trackerName.isEmpty()))
    properties["TRACKERNAME"] = d->trackerName;
  return properties;
}

PropertyMap Mod::Tag::setProperties(const PropertyMap &origProps)
{
  PropertyMap props(origProps);
  props.removeEmpty();
  StringList oneValueSet;
  if(props.contains("TITLE")) {
    d->title = props["TITLE"].front();
    oneValueSet.append("TITLE");
  } else
    d->title.clear();

  if(props.contains("COMMENT")) {
    d->comment = props["COMMENT"].front();
    oneValueSet.append("COMMENT");
  } else
    d->comment.clear();

  if(props.contains("TRACKERNAME")) {
    d->trackerName = props["TRACKERNAME"].front();
    oneValueSet.append("TRACKERNAME");
  } else
    d->trackerName.clear();

  // for each tag that has been set above, remove the first entry in the corresponding
  // value list. The others will be returned as unsupported by this format.
  for(const auto &entry : std::as_const(oneValueSet)) {
    if(props[entry].size() == 1)
      props.erase(entry);
    else
      props[entry].erase(props[entry].begin());
  }
  return props;
}
