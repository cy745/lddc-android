/***************************************************************************
    copyright           : (C) 2011 by Mathias Panzenböck
    email               : grosser.meister.morti@gmx.net
 ***************************************************************************/

/***************************************************************************
 *   This library is free software; you can redistribute it and/or modify  *
 *   it  under the terms of the GNU Lesser General Public License version  *
 *   2.1 as published by the Free Software Foundation.                     *
 *                                                                         *
 *   This library is distributed in the hope that it will be useful, but   *
 *   WITHOUT ANY WARRANTY; without even the implied warranty of            *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with this library; if not, write to the Free Software   *
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,            *
 *   MA  02110-1301  USA                                                   *
 ***************************************************************************/

#ifndef TAGLIB_ITFILE_H
#define TAGLIB_ITFILE_H

#include "tfile.h"
#include "taglib_export.h"
#include "audioproperties.h"
#include "modfilebase.h"
#include "modtag.h"
#include "itproperties.h"

namespace TagLib {

  namespace IT {

    class TAGLIB_EXPORT File : public Mod::FileBase {
      public:
        /*!
         * Constructs a Impulse Tracker file from \a file.
         *
         * \note In the current implementation, both \a readProperties and
         * \a propertiesStyle are ignored.  The audio properties are always
         * read.
         */
        File(FileName file, bool readProperties = true,
             AudioProperties::ReadStyle propertiesStyle =
             AudioProperties::Average);

        /*!
         * Constructs a Impulse Tracker file from \a stream.
         *
         * \note In the current implementation, both \a readProperties and
         * \a propertiesStyle are ignored.  The audio properties are always
         * read.
         *
         * \note TagLib will *not* take ownership of the stream, the caller is
         * responsible for deleting it after the File object.
         */
        File(IOStream *stream, bool readProperties = true,
             AudioProperties::ReadStyle propertiesStyle =
             AudioProperties::Average);

        /*!
         * Destroys this instance of the File.
         */
        ~File() override;

        File(const File &) = delete;
        File &operator=(const File &) = delete;

        Mod::Tag *tag() const override;

        /*!
         * Returns the IT::Properties for this file. If no audio properties
         * were read then this will return a null pointer.
         */
        IT::Properties *audioProperties() const override;

        /*!
         * Save the file.
         * This is the same as calling save(AllTags);
         *
         * \note Saving Impulse Tracker tags is not supported.
         */
        bool save() override;

      private:
        void read(bool readProperties);

        class FilePrivate;
        TAGLIB_MSVC_SUPPRESS_WARNING_NEEDS_TO_HAVE_DLL_INTERFACE
        std::unique_ptr<FilePrivate> d;
    };
  }  // namespace IT
}  // namespace TagLib

#endif
